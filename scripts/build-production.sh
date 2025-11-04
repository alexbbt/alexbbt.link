#!/bin/bash
# Build script for production deployment
# Builds Next.js static export and packages it into Spring Boot JAR

set -e

echo "🚀 Building URL Shortener for production..."

# Step 1: Build Next.js static export
echo "📦 Building Next.js frontend..."
cd frontend
export NEXT_STATIC_EXPORT=true
npm install
npm run build

# Verify build output
if [ ! -d "out" ]; then
    echo "❌ Error: Next.js build did not create 'out' directory"
    exit 1
fi

echo "✅ Next.js build complete"

# Step 2: Copy Next.js static files to Spring Boot resources
echo "📋 Copying static files to Spring Boot..."
cd ..
mkdir -p backend/src/main/resources/static/admin
rm -rf backend/src/main/resources/static/admin/*
# With basePath='/admin', Next.js outputs to out/admin/
cp -r frontend/out/admin/* backend/src/main/resources/static/admin/
cp -r frontend/out/_next backend/src/main/resources/static/admin/_next

echo "✅ Static files copied to Spring Boot resources"

# Step 3: Build Spring Boot JAR
echo "🔨 Building Spring Boot application..."
cd backend
./gradlew clean build -x test

echo "✅ Spring Boot build complete"
echo ""
echo "🎉 Production build complete!"
echo "📦 JAR file location: backend/build/libs/"
ls -lh backend/build/libs/*.jar
