#!/bin/bash

# Health Check Script for Bookitty Application
set -e

HEALTH_URL="http://localhost:9090/actuator/health"
BLUE_PORT=8080
GREEN_PORT=8081
SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to send Slack notification
send_slack_notification() {
    local message="$1"
    local color="$2"
    
    if [ -n "$SLACK_WEBHOOK_URL" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"attachments\":[{\"color\":\"$color\",\"text\":\"Bookitty Health Alert: $message\"}]}" \
            "$SLACK_WEBHOOK_URL" 2>/dev/null || true
    fi
}

# Function to check application health
check_health() {
    local port=$1
    local health_url="http://localhost:$((port + 1000))/actuator/health"
    
    log "Checking health on port $port (health endpoint: $health_url)"
    
    response=$(curl -s -w "%{http_code}" -o /tmp/health_response "$health_url" 2>/dev/null || echo "000")
    
    if [ "$response" = "200" ]; then
        status=$(cat /tmp/health_response | grep -o '"status":"[^"]*"' | cut -d'"' -f4 2>/dev/null || echo "UNKNOWN")
        if [ "$status" = "UP" ]; then
            log "Application on port $port is healthy"
            return 0
        else
            log "Application on port $port is unhealthy - Status: $status"
            return 1
        fi
    else
        log "Health check failed on port $port - HTTP Code: $response"
        return 1
    fi
}

# Function to get detailed health info
get_health_details() {
    local port=$1
    local health_url="http://localhost:$((port + 1000))/actuator/health"
    
    curl -s "$health_url" 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed to get health details"
}

# Function to check which container is currently active
get_active_container() {
    if docker ps --format "table {{.Names}}" | grep -q "^bookitty-blue$"; then
        if check_health $BLUE_PORT >/dev/null 2>&1; then
            echo "blue"
            return 0
        fi
    fi
    
    if docker ps --format "table {{.Names}}" | grep -q "^bookitty-green$"; then
        if check_health $GREEN_PORT >/dev/null 2>&1; then
            echo "green"
            return 0
        fi
    fi
    
    echo "none"
    return 1
}

# Main health check function
main_health_check() {
    log "Starting health check for Bookitty application"
    
    local healthy_containers=0
    local total_containers=0
    local active_container=""
    
    # Check Blue container
    if docker ps --format "table {{.Names}}" | grep -q "^bookitty-blue$"; then
        total_containers=$((total_containers + 1))
        if check_health $BLUE_PORT; then
            healthy_containers=$((healthy_containers + 1))
            active_container="blue"
        else
            log "Blue container health check failed"
            get_health_details $BLUE_PORT
        fi
    fi
    
    # Check Green container
    if docker ps --format "table {{.Names}}" | grep -q "^bookitty-green$"; then
        total_containers=$((total_containers + 1))
        if check_health $GREEN_PORT; then
            healthy_containers=$((healthy_containers + 1))
            if [ -z "$active_container" ]; then
                active_container="green"
            fi
        else
            log "Green container health check failed"
            get_health_details $GREEN_PORT
        fi
    fi
    
    # Report results
    if [ $total_containers -eq 0 ]; then
        log "ERROR: No Bookitty containers are running"
        send_slack_notification "No Bookitty containers are running" "danger"
        return 1
    elif [ $healthy_containers -eq 0 ]; then
        log "ERROR: All Bookitty containers are unhealthy ($total_containers total)"
        send_slack_notification "All Bookitty containers are unhealthy ($total_containers total)" "danger"
        return 1
    elif [ $healthy_containers -lt $total_containers ]; then
        log "WARNING: Some containers are unhealthy ($healthy_containers/$total_containers healthy)"
        send_slack_notification "Some Bookitty containers are unhealthy ($healthy_containers/$total_containers healthy)" "warning"
        return 1
    else
        log "SUCCESS: All containers are healthy ($healthy_containers/$total_containers)"
        return 0
    fi
}

# Continuous monitoring mode
monitor_mode() {
    local interval=${1:-60}  # Default 60 seconds
    log "Starting continuous monitoring mode (interval: ${interval}s)"
    
    while true; do
        if ! main_health_check; then
            log "Health check failed, waiting $interval seconds before retry..."
        fi
        sleep $interval
    done
}

# Usage information
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -h, --help          Show this help message"
    echo "  -m, --monitor INT   Continuous monitoring mode with interval (default: 60s)"
    echo "  -d, --details       Show detailed health information"
    echo "  -a, --active        Show currently active container"
    echo ""
    echo "Examples:"
    echo "  $0                  # Single health check"
    echo "  $0 -m 30           # Monitor every 30 seconds"
    echo "  $0 --details       # Show detailed health info"
    echo "  $0 --active        # Show active container"
}

# Parse command line arguments
case "${1:-}" in
    -h|--help)
        usage
        exit 0
        ;;
    -m|--monitor)
        monitor_mode "${2:-60}"
        ;;
    -d|--details)
        log "Getting detailed health information..."
        active=$(get_active_container)
        if [ "$active" = "blue" ]; then
            get_health_details $BLUE_PORT
        elif [ "$active" = "green" ]; then
            get_health_details $GREEN_PORT
        else
            log "No healthy container found"
            exit 1
        fi
        ;;
    -a|--active)
        active=$(get_active_container)
        if [ "$active" != "none" ]; then
            log "Currently active container: $active"
            echo "$active"
        else
            log "No active container found"
            exit 1
        fi
        ;;
    "")
        main_health_check
        ;;
    *)
        echo "Unknown option: $1"
        usage
        exit 1
        ;;
esac