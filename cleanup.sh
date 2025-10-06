#!/bin/bash

echo "🧹 Starting cleanup process..."

# Stop all running containers
echo "Stopping all containers..."
docker compose down

# Remove all unused containers, networks, images and volumes
echo "Removing unused Docker resources..."
docker system prune -af
docker volume prune -f

# Clean npm cache (if exists)
if [ -d "frontend/node_modules" ]; then
    echo "Cleaning npm cache..."
    cd frontend
    npm cache clean --force
    rm -rf node_modules
    rm -rf build
    cd ..
fi

# Clean Maven cache (if exists)
if [ -d "backend/target" ]; then
    echo "Cleaning Maven cache..."
    cd backend
    mvn clean
    rm -rf target
    cd ..
fi

# Remove environment files (optional)
echo "Cleaning environment files..."
find . -name ".env" -type f -delete
find . -name ".env.*" -type f -delete

# Remove IDE specific files (optional)
echo "Cleaning IDE files..."
find . -name "*.iml" -type f -delete
find . -name ".idea" -type d -exec rm -rf {} +
find . -name ".vscode" -type d -exec rm -rf {} +

echo "✨ Cleanup completed!"