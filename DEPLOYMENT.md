# URL Shortener Deployment Guide

## Quick Start

### Local Development

1. **Start services:**
   ```bash
   ./sail up
   ```

2. **Start backend:**
   ```bash
   ./sail backend
   ```

3. **Start frontend (in another terminal):**
   ```bash
   ./sail frontend
   ```

4. **Access:**
   - Frontend (Admin): http://localhost:3000/admin
   - Backend API: http://localhost:8080/api
   - Test redirect: http://localhost:8080/{slug}

### Production Build

#### Option 1: Docker Compose (Recommended)

```bash
docker compose -f docker-compose.prod.yml up --build
```

This will:
- Build the production Docker image
- Start all services (app, db, redis)
- Run everything in the background

#### Option 2: Docker Build

```bash
docker build -f Dockerfile.prod -t url-shortener:latest .
```

This only builds the image; you'll need to run services separately.

## Production Deployment

### Testing Single Container Setup

Before deploying to k3s, test the single container locally:

```bash
docker-compose -f docker-compose.prod.yml up --build
```

This builds the production image and runs the full stack (app, PostgreSQL, Redis) locally.
Access at: http://localhost:8080/admin

### Single Container Approach

The application uses a **single container** approach where:
- Next.js is built as static files and embedded in Spring Boot
- Spring Boot serves everything (admin UI, API, redirects)
- No reverse proxy needed for basic deployments
- Perfect for k3s/Kubernetes deployment

### Environment Variables

Set these environment variables for production:

```bash
BASE_URL=https://alexbbt.link
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/app
SPRING_DATASOURCE_USERNAME=app
SPRING_DATASOURCE_PASSWORD=your-secure-password
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
```

### Running the JAR

```bash
java -jar backend/build/libs/webserver-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --BASE_URL=https://alexbbt.link
```

### Docker Run

```bash
docker run -d \
  -p 8080:8080 \
  -e BASE_URL=https://alexbbt.link \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name url-shortener \
  url-shortener:latest
```

## Architecture

### Routes

- `/admin` - Admin interface (Next.js static files)
- `/admin/*` - Admin interface pages
- `/api/shortlinks` - API endpoints
- `/{slug}` - Short link redirects (root-level)

### Database Schema

The `short_links` table is created automatically via JPA/Hibernate.

### Caching

- Redis caches active short links for 24 hours
- Cache key format: `shortlink:{slug}`
- Cache is automatically invalidated on updates/deletes

## Testing

### Test Redirect

1. Create a short link via admin interface
2. Visit `http://localhost:8080/{slug}`
3. Should redirect to original URL

### Test API

```bash
# Create a short link
curl -X POST http://localhost:8080/api/shortlinks \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com", "slug": "test"}'

# Get all links
curl http://localhost:8080/api/shortlinks

# Get stats
curl http://localhost:8080/api/shortlinks/stats
```

## Troubleshooting

### Build Fails

- Ensure Node.js 22+ is installed
- Check that `frontend/out` directory is created after Next.js build
- Verify Java 17 is available

### Redirects Don't Work

- Check that `/{slug}` route isn't being caught by static file handler
- Verify Redis is running and accessible
- Check application logs for errors

### Admin Interface Not Loading

- Verify static files are in `backend/src/main/resources/static/admin/`
- Check that `basePath: '/admin'` is set in Next.js config
- Ensure Spring Boot static resource handler is configured

## Production Checklist

- [ ] Set `BASE_URL` environment variable
- [ ] Configure database credentials
- [ ] Set up Redis persistence
- [ ] Configure SSL/TLS (if using HTTPS)
- [ ] Set up monitoring and logging
- [ ] Configure backups for database
- [ ] Test redirect functionality
- [ ] Test admin interface
- [ ] Load test for performance
