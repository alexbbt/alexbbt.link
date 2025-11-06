#!/bin/bash
# Script to test production Docker build locally

set -e

echo "🏗️  Building production Docker image..."
docker compose -f docker-compose.prod.yml build

echo ""
echo "🚀 Starting production services..."
docker compose -f docker-compose.prod.yml up -d

echo ""
echo "⏳ Waiting for services to be healthy..."
echo "   Checking database..."
until docker compose -f docker-compose.prod.yml exec -T db pg_isready -U app -d app > /dev/null 2>&1; do
    sleep 1
done
echo "   ✅ Database ready"

echo "   Checking application..."
for i in {1..60}; do
    if curl -sf http://localhost:8080/api/health > /dev/null 2>&1; then
        echo "   ✅ Application ready"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "   ❌ Application failed to start"
        echo ""
        echo "📋 Application logs:"
        docker compose -f docker-compose.prod.yml logs app
        exit 1
    fi
    sleep 2
done

echo ""
echo "✅ Production environment is running!"
echo ""
echo "📍 Access points:"
echo "   - Application: http://localhost:8080"
echo "   - Admin UI:    http://localhost:8080/admin"
echo "   - Health:      http://localhost:8080/api/health"
echo ""
echo "👤 Create a user:"
echo "   docker compose -f docker-compose.prod.yml exec app java -jar app.jar --create-user"
echo ""
echo "📋 View logs:"
echo "   docker compose -f docker-compose.prod.yml logs -f app"
echo ""
echo "🛑 Stop services:"
echo "   docker compose -f docker-compose.prod.yml down"
