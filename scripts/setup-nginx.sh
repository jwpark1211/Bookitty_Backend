#!/bin/bash

# Setup Nginx for Bookitty application
set -e

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root or with sudo"
    exit 1
fi

log "Setting up Nginx for Bookitty application"

# Install Nginx if not already installed
if ! command -v nginx &> /dev/null; then
    log "Installing Nginx"
    apt-get update
    apt-get install -y nginx
else
    log "Nginx is already installed"
fi

# Create necessary directories
log "Creating Nginx directories"
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled
mkdir -p /var/cache/nginx
mkdir -p /var/log/nginx

# Backup existing nginx.conf
if [ -f /etc/nginx/nginx.conf ]; then
    log "Backing up existing nginx.conf"
    cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup.$(date +%Y%m%d_%H%M%S)
fi

# Copy configuration files
log "Copying Nginx configuration files"
cp nginx/nginx.conf /etc/nginx/nginx.conf
cp nginx/bookitty /etc/nginx/sites-available/bookitty

# Enable the site
log "Enabling Bookitty site"
ln -sf /etc/nginx/sites-available/bookitty /etc/nginx/sites-enabled/bookitty

# Remove default site if it exists
if [ -f /etc/nginx/sites-enabled/default ]; then
    log "Removing default Nginx site"
    rm -f /etc/nginx/sites-enabled/default
fi

# Set proper permissions
log "Setting proper permissions"
chown -R nginx:nginx /var/cache/nginx
chown -R nginx:nginx /var/log/nginx

# Test Nginx configuration
log "Testing Nginx configuration"
if nginx -t; then
    log "Nginx configuration test passed"
    
    # Enable and start Nginx
    systemctl enable nginx
    systemctl restart nginx
    
    log "Nginx has been configured and started successfully"
    log "Don't forget to:"
    log "1. Update server_name in /etc/nginx/sites-available/bookitty"
    log "2. Configure SSL/TLS certificates (recommended: Let's Encrypt)"
    log "3. Update firewall rules to allow HTTP(80) and HTTPS(443)"
else
    log "Nginx configuration test failed"
    exit 1
fi