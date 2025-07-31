#!/bin/bash

# Rollback Script for Blue-Green Deployment
set -e

BLUE_PORT=8080
GREEN_PORT=8081
NGINX_CONFIG_DIR="/etc/nginx/sites-available"
HEALTH_CHECK_URL="http://localhost"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to check if container is running
is_container_running() {
    local container_name=$1
    docker ps --format "table {{.Names}}" | grep -q "^${container_name}$"
}

# Function to check application health
check_health() {
    local port=$1
    local max_attempts=10
    local attempt=1
    
    log "Checking health on port $port"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f "${HEALTH_CHECK_URL}:${port}/actuator/health" >/dev/null 2>&1; then
            log "Health check passed on port $port"
            return 0
        fi
        
        log "Health check attempt $attempt/$max_attempts failed on port $port, waiting..."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    log "Health check failed on port $port after $max_attempts attempts"
    return 1
}

# Function to update Nginx configuration
update_nginx_config() {
    local port=$1
    log "Updating Nginx configuration to use port $port"
    
    # Update upstream configuration
    sudo sed -i "s/server localhost:[0-9]\+/server localhost:$port/" $NGINX_CONFIG_DIR/bookitty
    
    # Test Nginx configuration
    if sudo nginx -t; then
        sudo systemctl reload nginx
        log "Nginx configuration updated and reloaded"
    else
        log "Nginx configuration test failed"
        return 1
    fi
}

# Main rollback logic
main() {
    log "Starting rollback process"
    
    # Determine which container to rollback to
    local rollback_port=""
    local rollback_container=""
    local current_port=""
    
    # Check current Nginx configuration to determine active port
    current_port=$(grep -oP 'server localhost:\K[0-9]+' $NGINX_CONFIG_DIR/bookitty | head -1)
    
    if [ "$current_port" = "$BLUE_PORT" ]; then
        rollback_port=$GREEN_PORT
        rollback_container="bookitty-green"
        log "Current active: Blue ($BLUE_PORT), rolling back to Green ($GREEN_PORT)"
    elif [ "$current_port" = "$GREEN_PORT" ]; then
        rollback_port=$BLUE_PORT
        rollback_container="bookitty-blue"
        log "Current active: Green ($GREEN_PORT), rolling back to Blue ($BLUE_PORT)"
    else
        log "Cannot determine current active container from Nginx config"
        exit 1
    fi
    
    # Check if rollback target container is running and healthy
    if is_container_running $rollback_container; then
        if check_health $rollback_port; then
            log "Rollback target container is healthy, switching traffic"
            
            if update_nginx_config $rollback_port; then
                log "Rollback completed successfully"
                log "Traffic is now routed to $rollback_container on port $rollback_port"
            else
                log "Failed to update Nginx configuration during rollback"
                exit 1
            fi
        else
            log "Rollback target container is not healthy"
            exit 1
        fi
    else
        log "Rollback target container $rollback_container is not running"
        log "Cannot perform rollback without a healthy previous version"
        exit 1
    fi
}

# Run main function
main "$@"