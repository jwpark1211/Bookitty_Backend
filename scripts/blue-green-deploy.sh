#!/bin/bash

# Blue-Green Deployment Script for Bookitty Application
set -e

DOCKER_IMAGE=$1
TAG=${2:-latest}
BLUE_PORT=8080
GREEN_PORT=8081
NGINX_CONFIG_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"
HEALTH_CHECK_URL="http://localhost"
MAX_WAIT_TIME=120

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Load Docker image if tar.gz exists
if [ -f "${DOCKER_IMAGE}.tar.gz" ]; then
    log "Loading Docker image from ${DOCKER_IMAGE}.tar.gz"
    docker load < "${DOCKER_IMAGE}.tar.gz"
    rm -f "${DOCKER_IMAGE}.tar.gz"
fi

# Function to check if container is running
is_container_running() {
    local container_name=$1
    docker ps --format "table {{.Names}}" | grep -q "^${container_name}$"
}

# Function to check application health
check_health() {
    local port=$1
    local max_attempts=20
    local attempt=1
    
    log "Checking health on port $port"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f "${HEALTH_CHECK_URL}:${port}/actuator/health" >/dev/null 2>&1; then
            log "Health check passed on port $port"
            return 0
        fi
        
        log "Health check attempt $attempt/$max_attempts failed on port $port, waiting..."
        sleep 6
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

# Function to stop and remove container
cleanup_container() {
    local container_name=$1
    if is_container_running $container_name; then
        log "Stopping container $container_name"
        docker stop $container_name
    fi
    
    if docker ps -a --format "table {{.Names}}" | grep -q "^${container_name}$"; then
        log "Removing container $container_name"
        docker rm $container_name
    fi
}

# Main deployment logic
main() {
    log "Starting Blue-Green deployment for $DOCKER_IMAGE:$TAG"
    
    # Determine current active service and target service
    local current_port=""
    local target_port=""
    local current_container=""
    local target_container=""
    
    if is_container_running "bookitty-blue"; then
        current_port=$BLUE_PORT
        target_port=$GREEN_PORT
        current_container="bookitty-blue"
        target_container="bookitty-green"
        log "Blue is currently active, deploying to Green"
    elif is_container_running "bookitty-green"; then
        current_port=$GREEN_PORT
        target_port=$BLUE_PORT
        current_container="bookitty-green"
        target_container="bookitty-blue"
        log "Green is currently active, deploying to Blue"
    else
        # Initial deployment
        target_port=$BLUE_PORT
        target_container="bookitty-blue"
        log "No active container found, initial deployment to Blue"
    fi
    
    # Cleanup any existing target container
    cleanup_container $target_container
    
    # Start new container
    log "Starting new container $target_container on port $target_port"
    docker run -d \
        --name $target_container \
        --restart unless-stopped \
        -p $target_port:8080 \
        -p $((target_port + 1000)):9090 \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e DB_META_URL="${DB_META_URL}" \
        -e DB_META_USERNAME="${DB_META_USERNAME}" \
        -e DB_META_PASSWORD="${DB_META_PASSWORD}" \
        -e DB_DATA_URL="${DB_DATA_URL}" \
        -e DB_DATA_USERNAME="${DB_DATA_USERNAME}" \
        -e DB_DATA_PASSWORD="${DB_DATA_PASSWORD}" \
        -e REDIS_HOST="${REDIS_HOST}" \
        -e REDIS_PORT="${REDIS_PORT}" \
        -e REDIS_PASSWORD="${REDIS_PASSWORD}" \
        -e JWT_SECRET_KEY="${JWT_SECRET_KEY}" \
        -e ALADIN_TTB_KEY="${ALADIN_TTB_KEY}" \
        -e SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL}" \
        -v /var/log/bookitty:/var/log/bookitty \
        $DOCKER_IMAGE:$TAG
    
    # Wait for new container to be healthy
    if check_health $target_port; then
        log "New container is healthy, switching traffic"
        
        # Update Nginx to point to new container
        if update_nginx_config $target_port; then
            log "Traffic switched successfully"
            
            # Wait a bit for traffic to drain from old container
            if [ -n "$current_container" ]; then
                log "Waiting for traffic to drain from old container..."
                sleep 30
                
                # Stop old container
                cleanup_container $current_container
                log "Old container $current_container stopped and removed"
            fi
            
            log "Blue-Green deployment completed successfully"
        else
            log "Failed to update Nginx configuration, rolling back"
            cleanup_container $target_container
            exit 1
        fi
    else
        log "New container failed health check, rolling back"
        cleanup_container $target_container
        exit 1
    fi
    
    # Clean up old Docker images (keep last 3)
    log "Cleaning up old Docker images"
    docker images $DOCKER_IMAGE --format "table {{.ID}}" | tail -n +4 | xargs -r docker rmi || true
    
    log "Deployment completed successfully"
}

# Check if required environment variables are set
required_vars="DB_META_URL DB_META_USERNAME DB_META_PASSWORD DB_DATA_URL DB_DATA_USERNAME DB_DATA_PASSWORD REDIS_HOST JWT_SECRET_KEY ALADIN_TTB_KEY SLACK_WEBHOOK_URL"
for var in $required_vars; do
    if [ -z "${!var}" ]; then
        log "Error: Required environment variable $var is not set"
        exit 1
    fi
done

# Run main function
main "$@"