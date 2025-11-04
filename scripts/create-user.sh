#!/bin/bash
# Script to create a user via the CreateUserCommand in the dev container

set -e

DC="docker compose"
APP_SERVICE="app"
JAR_NAME="webserver-0.0.1-SNAPSHOT.jar"

# Check if Docker daemon is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker daemon is not running!"
    echo "Please start Docker Desktop or Docker daemon and try again."
    exit 1
fi

# Check if containers are running
if ! $DC ps --format "{{.Name}}" 2>/dev/null | grep -qE "(app|db|redis)"; then
    echo "📦 No Spring Sail containers are running"
    echo "🚀 Starting development environment..."
    $DC up -d
    echo "⏳ Waiting for services to be ready..."
    sleep 5
fi

echo "🔨 Building JAR if needed..."
# Build JAR (skip tests and spotbugs for faster build)
$DC exec "$APP_SERVICE" bash -c "cd backend && ./gradlew build -x test -x spotbugsMain -x spotbugsTest" || {
    echo "❌ Failed to build JAR"
    exit 1
}

# Check if JAR exists
if ! $DC exec "$APP_SERVICE" bash -c "cd backend && test -f build/libs/$JAR_NAME"; then
    echo "❌ JAR file not found at backend/build/libs/$JAR_NAME"
    exit 1
fi

echo "👤 Running create-user command..."
echo ""

# Run the command interactively
$DC exec -it "$APP_SERVICE" bash -c "cd backend && java -jar build/libs/$JAR_NAME --create-user --spring.main.web-application-type=none"
