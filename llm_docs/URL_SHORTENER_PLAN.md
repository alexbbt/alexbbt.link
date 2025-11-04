# URL Shortener Implementation Plan

## Overview
Build a super fast and performant URL shortener service hosted on `alexbbt.link` with:
- Database persistence for all redirects (PostgreSQL)
- In-memory caching for active/recently used links (Redis)
- React admin interface at `/admin` for link management
- Root-level redirects for all short links (e.g., `alexbbt.link/my-link`)

## Architecture

### Current Stack
- **Backend**: Spring Boot 3.5.6 with Java 17
- **Frontend**: Next.js 15.5.4 with React 19
- **Database**: PostgreSQL 16 (already configured)
- **Cache**: Redis 7 (already configured)
- **Domain**: `alexbbt.link`

### Key Requirements
1. **Performance**: Sub-millisecond redirect lookups using Redis cache
2. **Persistence**: All links stored in PostgreSQL
3. **Admin Interface**: React frontend at `/admin` for creating/managing links
4. **Routing**: Root-level routes for short links (Next.js catch-all)
5. **Link Format**:
   - Custom slugs: `alexbbt.link/my-link`
   - Random 6-character codes: `alexbbt.link/abc123`

## Implementation Plan

### Phase 1: Backend - Database & Models

#### 1.1 Database Schema
**Table: `short_links`**
```sql
CREATE TABLE short_links (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(50) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    click_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(255) -- Optional: for future user management
);

CREATE INDEX idx_slug ON short_links(slug);
CREATE INDEX idx_created_at ON short_links(created_at);
CREATE INDEX idx_active ON short_links(is_active) WHERE is_active = TRUE;
```

#### 1.2 Entity Model
- **File**: `backend/src/main/java/com/hna/webserver/model/ShortLink.java`
- Fields: id, slug, originalUrl, clickCount, createdAt, updatedAt, expiresAt, isActive
- JPA annotations with proper validation
- Auto-generate timestamps

#### 1.3 Repository Layer
- **File**: `backend/src/main/java/com/hna/webserver/repository/ShortLinkRepository.java`
- Extend `JpaRepository<ShortLink, Long>`
- Custom query methods:
  - `findBySlug(String slug)`
  - `existsBySlug(String slug)`
  - `findByIsActiveTrueOrderByUpdatedAtDesc()` (for cache warming)

### Phase 2: Backend - Service Layer

#### 2.1 ShortLinkService
- **File**: `backend/src/main/java/com/hna/webserver/service/ShortLinkService.java`
- **Responsibilities**:
  - Generate unique slugs (6-character random or custom)
  - Validate URLs
  - Create short links
  - Retrieve redirect URLs (with caching)
  - Track click counts
  - Handle expired links

#### 2.2 Slug Generation Strategy
- **Custom slugs**: User-provided (validate uniqueness)
- **Random slugs**: 6 characters using base62 (0-9, a-z, A-Z)
- Collision handling: Retry with new random slug
- Reserved slugs: `admin`, `api`, `_next`, `favicon.ico`, etc.

#### 2.3 Caching Strategy
- **Redis Key Format**: `shortlink:{slug}`
- **Cache TTL**: 24 hours (configurable)
- **Cache Warming**: On service startup, load top 1000 most recently used links
- **Cache Invalidation**: On link updates/deletions
- **Cache-Aside Pattern**: Check Redis first, fallback to DB, then cache result

### Phase 3: Backend - API Controllers

#### 3.1 Redirect Controller
- **File**: `backend/src/main/java/com/hna/webserver/controller/RedirectController.java`
- **Endpoint**: `GET /{slug}` (root level, not under `/api`)
- **Logic**:
  1. Check Redis cache
  2. If not found, query database
  3. Validate link (active, not expired)
  4. Increment click count (async)
  5. Cache the result
  6. Return 302 redirect to original URL
  7. Handle 404 for invalid/expired links

#### 3.2 Admin API Controller
- **File**: `backend/src/main/java/com/hna/webserver/controller/ShortLinkController.java`
- **Base Path**: `/api/shortlinks`
- **Endpoints**:
  - `POST /api/shortlinks` - Create short link
    - Request: `{ url: string, slug?: string }`
    - Response: `{ slug: string, shortUrl: string, originalUrl: string, createdAt: string }`
  - `GET /api/shortlinks` - List all links (paginated)
    - Query params: `?page=0&size=20&sort=createdAt,desc`
  - `GET /api/shortlinks/{slug}` - Get link details
  - `PUT /api/shortlinks/{slug}` - Update link
  - `DELETE /api/shortlinks/{slug}` - Delete link
  - `GET /api/shortlinks/stats` - Get aggregate statistics

### Phase 4: Backend - Configuration & Dependencies

#### 4.1 Add Redis Dependencies
- Update `build.gradle`:
  ```gradle
  implementation 'org.springframework.boot:spring-boot-starter-data-redis'
  implementation 'org.springframework.boot:spring-boot-starter-cache'
  ```

#### 4.2 Redis Configuration
- **File**: `backend/src/main/java/com/hna/webserver/config/RedisConfig.java`
- Configure RedisTemplate for String operations
- Enable caching with `@EnableCaching`
- Configure cache manager

#### 4.3 URL Validation
- **File**: `backend/src/main/java/com/hna/webserver/util/UrlValidator.java`
- Validate URL format
- Ensure protocol (http/https)
- Reject localhost/internal IPs (optional security)

### Phase 5: Frontend - Admin Interface

#### 5.1 Admin Route Structure
- **File**: `frontend/src/app/admin/page.tsx` - Main admin page
- **File**: `frontend/src/app/admin/layout.tsx` - Admin layout with navigation
- **File**: `frontend/src/app/admin/links/page.tsx` - Link list view
- **File**: `frontend/src/app/admin/links/[slug]/page.tsx` - Link detail/edit

#### 5.2 Admin Components
- **File**: `frontend/src/components/admin/CreateLinkForm.tsx`
  - URL input
  - Optional custom slug input
  - Generate random slug button
  - Validation and error handling
- **File**: `frontend/src/components/admin/LinkList.tsx`
  - Table/list of all links
  - Search/filter functionality
  - Pagination
  - Click count display
  - Copy short URL button
- **File**: `frontend/src/components/admin/LinkStats.tsx`
  - Total links
  - Total clicks
  - Most clicked links
  - Recent activity

#### 5.3 API Integration
- **File**: `frontend/src/lib/api.ts` (update)
- Add shortlink API functions:
  ```typescript
  createShortLink(url: string, slug?: string)
  getShortLinks(page?: number, size?: number)
  getShortLink(slug: string)
  updateShortLink(slug: string, data: UpdateShortLinkDto)
  deleteShortLink(slug: string)
  getShortLinkStats()
  ```

### Phase 6: Frontend - Root-Level Redirects

#### 6.1 Catch-All Route
- **File**: `frontend/src/app/[[...slug]]/page.tsx`
- **Purpose**: Catch all root-level routes that aren't admin or Next.js routes
- **Logic**:
  1. Check if route is reserved (admin, api, _next, etc.) → 404
  2. Call backend API to get redirect URL
  3. If found, redirect client-side or server-side
  4. If not found, show 404 page

#### 6.2 Reserved Routes
- `/admin` - Admin interface
- `/api` - API routes (Next.js)
- `/_next` - Next.js internal routes
- `/favicon.ico` - Favicon
- Any route starting with `_` - Next.js special routes

### Phase 7: Performance Optimizations

#### 7.1 Backend Optimizations
- Connection pooling for PostgreSQL
- Redis connection pooling
- Async click count updates (fire and forget)
- Batch operations for cache warming
- Database query optimization (indexes)

#### 7.2 Frontend Optimizations
- Server-side redirects for better SEO
- Prefetch popular links
- Client-side caching for admin interface
- Optimistic UI updates

#### 7.3 Caching Strategy Refinement
- Cache hit rate monitoring
- LRU eviction for Redis
- Background cache refresh for hot links
- Cache TTL based on link popularity

### Phase 8: Security & Validation

#### 8.1 Input Validation
- URL format validation
- Slug format validation (alphanumeric + hyphens)
- Rate limiting (future)
- XSS protection on admin interface

#### 8.2 Security Headers
- Add security headers to redirects
- Validate redirect URLs to prevent open redirects
- Optional: Block malicious URLs

#### 8.3 Admin Authentication (Future)
- Basic auth or JWT for `/admin` route
- API key or OAuth for production

### Phase 9: Analytics & Monitoring

#### 9.1 Click Tracking
- Async click count updates
- Optional: Detailed click analytics (IP, referrer, timestamp)
- Database table for click logs (optional)

#### 9.2 Admin Dashboard
- Total links created
- Total clicks
- Most popular links
- Recent activity feed
- Link expiration warnings

### Phase 10: Testing

#### 10.1 Backend Tests
- Unit tests for service layer
- Integration tests for controllers
- Redis cache tests
- Database tests

#### 10.2 Frontend Tests
- Component tests for admin interface
- E2E tests for link creation flow
- Redirect functionality tests

## File Structure

```
backend/
├── src/main/java/com/hna/webserver/
│   ├── model/
│   │   └── ShortLink.java
│   ├── repository/
│   │   └── ShortLinkRepository.java
│   ├── service/
│   │   ├── ShortLinkService.java
│   │   └── CacheService.java
│   ├── controller/
│   │   ├── RedirectController.java
│   │   └── ShortLinkController.java
│   ├── config/
│   │   └── RedisConfig.java
│   ├── util/
│   │   ├── UrlValidator.java
│   │   └── SlugGenerator.java
│   └── dto/
│       ├── CreateShortLinkRequest.java
│       ├── ShortLinkResponse.java
│       └── UpdateShortLinkRequest.java

frontend/
├── src/app/
│   ├── admin/
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   └── links/
│   │       ├── page.tsx
│   │       └── [slug]/page.tsx
│   └── [[...slug]]/
│       └── page.tsx
├── src/components/admin/
│   ├── CreateLinkForm.tsx
│   ├── LinkList.tsx
│   ├── LinkStats.tsx
│   └── LinkCard.tsx
└── src/lib/
    └── api.ts (updated)
```

## Implementation Order

1. **Backend Foundation** (Phase 1-2)
   - Database schema and entity
   - Repository layer
   - Service layer with slug generation

2. **Backend API** (Phase 3-4)
   - Redirect controller
   - Admin API controller
   - Redis integration

3. **Frontend Admin** (Phase 5)
   - Admin interface components
   - API integration
   - Link management UI

4. **Frontend Redirects** (Phase 6)
   - Catch-all route
   - Redirect logic

5. **Optimization** (Phase 7)
   - Performance tuning
   - Caching refinement

6. **Security & Polish** (Phase 8-9)
   - Validation
   - Analytics
   - Error handling

7. **Testing** (Phase 10)
   - Unit tests
   - Integration tests

## Configuration Updates

### Backend `application-dev.yml`
```yaml
spring:
  # ... existing config ...
  cache:
    type: redis
    redis:
      time-to-live: 86400000  # 24 hours in ms

shortlink:
  default-slug-length: 6
  cache-ttl-hours: 24
  reserved-slugs: admin,api,_next,favicon.ico
```

### Next.js `next.config.ts`
- Add rewrites for root-level redirects
- Ensure `/admin` routes are handled correctly
- Configure catch-all route properly

## Deployment Architecture

### Local Development (Current Setup)
- **Frontend**: `localhost:3000` (Next.js dev server, runs locally)
- **Backend**: `localhost:8080` (Spring Boot, runs in Docker)
- **Why separate**: Fast hot reload for frontend, consistent environment for backend
- **API Communication**: Next.js rewrites `/api/*` to `http://localhost:8080/api`
- **CORS**: Configured to allow `localhost:3000` → `localhost:8080`

✅ **Recommendation**: Continue with this setup for local development. It provides the best developer experience with hot reload and clear separation.

### Production Architecture (`alexbbt.link`)

After researching deployment options and considering your preference for a single container (similar to Laravel Sail's approach), here are the viable options:

#### Option 1: Spring Boot Serves Static Next.js (Recommended) ⭐

**Single Container, Single Service - Similar to Laravel Sail**

```
Internet → Spring Boot (Port 8080) → Serves everything
                            ├─ /admin* → Static Next.js build (from resources/static)
                            ├─ /api/* → REST API
                            └─ /* → Redirect controller (fast Redis lookup)
```

**How It Works:**
1. Build Next.js with static export for `/admin` routes only
2. Copy static files to Spring Boot's `src/main/resources/static/admin`
3. Package everything into a single JAR file
4. Spring Boot serves static files from `/admin/*` automatically
5. Spring Boot handles all root routes (`/{slug}`) for redirects
6. Spring Boot handles `/api/*` for API endpoints

**Benefits:**
- ✅ **Single container** - One Docker container, one JAR file
- ✅ **No Node.js runtime needed** - Only Java/JVM in production
- ✅ **No reverse proxy needed** - Spring Boot handles everything
- ✅ **Fast redirects** - Direct Redis lookup, no extra hops
- ✅ **Simple deployment** - Deploy one artifact
- ✅ **Similar to Laravel Sail** - Single service approach
- ✅ **Cost effective** - Fewer resources needed

**Trade-offs:**
- ⚠️ Admin interface is fully static (no SSR/ISR) - acceptable for admin panel
- ⚠️ No Next.js server-side features - but admin doesn't need them
- ⚠️ Rebuild required for frontend changes - standard for production

**Implementation Details:**
- Next.js static export configured for `/admin` routes
- Multi-stage Docker build: build Next.js → copy to Spring Boot → build JAR
- Spring Boot static resource serving with proper routing
- No reverse proxy layer needed

#### Option 2: Single Container with Node.js Runtime (Laravel Sail Style)

**Single Container with Both Java and Node.js**

```
Container: Java + Node.js
├─ Next.js Standalone Server (Port 3000) → /admin routes
├─ Spring Boot (Port 8080) → /api/* and /{slug} routes
└─ Lightweight Proxy (Traefik/Nginx) → Routes based on path
```

**How It Works:**
1. Container includes both Java and Node.js runtimes
2. Next.js runs in standalone mode for `/admin` (enables SSR/ISR)
3. Spring Boot runs for API and redirects
4. Internal reverse proxy (Traefik or Nginx) routes requests

**Benefits:**
- ✅ Full Next.js features (SSR, ISR, API routes)
- ✅ Single container deployment
- ✅ Can update frontend/backend independently
- ✅ Most similar to Laravel Sail architecture

**Drawbacks:**
- ❌ Larger container size (Java + Node.js)
- ❌ More complex setup (proxy + two services)
- ❌ Higher resource usage
- ❌ More moving parts to manage

**When to Use:**
- If you need Next.js SSR/ISR features for `/admin`
- If you want to update frontend without rebuilding backend
- If container size isn't a concern

#### Option 3: Reverse Proxy (Traditional Approach)

**Nginx/Traefik Routes to Separate Services**

```
Internet → Reverse Proxy (Port 80/443) → Routes requests
                                   ├─ /admin* → Next.js (Port 3000 or static)
                                   ├─ /api/* → Spring Boot (Port 8080)
                                   └─ /* → Spring Boot (Port 8080)
```

**Benefits:**
- ✅ Industry standard pattern
- ✅ Services can be scaled independently
- ✅ Best for high-traffic scenarios

**Drawbacks:**
- ❌ Requires reverse proxy setup
- ❌ More complex infrastructure
- ❌ Additional layer (latency)

**When to Use:**
- High-traffic production with separate scaling needs
- When using container orchestration (Kubernetes)
- When you need advanced load balancing

### Recommended Production Setup

**Use Option 1 (Spring Boot Serves Static Next.js)** - Best fit for your requirements:

**Why Option 1 is Best:**
1. **Single container** - Matches your preference
2. **No Node.js in production** - Simpler, smaller, faster
3. **Admin interface doesn't need SSR** - Static is sufficient
4. **Fast redirects** - Direct Spring Boot path, no proxy overhead
5. **Simple deployment** - One JAR, one container, done
6. **Similar to Laravel Sail philosophy** - Single service, unified deployment

#### Implementation: Option 1 (Recommended)

**1. Next.js Configuration for Static Export**

Update `frontend/next.config.ts`:
```typescript
const nextConfig: NextConfig = {
  output: 'export', // Enable static export
  trailingSlash: true,
  basePath: '/admin', // All routes under /admin
  assetPrefix: '/admin', // Static assets under /admin

  // Environment variables
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'https://alexbbt.link',
    NEXT_PUBLIC_API_BASE: process.env.NEXT_PUBLIC_API_BASE || '/api',
  },
};
```

**2. Build Script for Production**

Create `scripts/build-production.sh`:
```bash
#!/bin/bash
# Build Next.js static export
cd frontend
npm install
npm run build  # Creates 'out' directory

# Copy Next.js static files to Spring Boot resources
cd ..
mkdir -p backend/src/main/resources/static/admin
cp -r frontend/out/* backend/src/main/resources/static/admin/

# Build Spring Boot JAR
cd backend
./gradlew clean build -x test
```

**3. Multi-Stage Dockerfile**

Create `Dockerfile.prod`:
```dockerfile
# Stage 1: Build Next.js frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot backend
FROM gradle:8-jdk17 AS backend-build
WORKDIR /app
COPY backend/ ./backend/
# Copy Next.js static files to Spring Boot resources
COPY --from=frontend-build /app/frontend/out ./backend/src/main/resources/static/admin/
RUN cd backend && ./gradlew clean build -x test

# Stage 3: Production runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**4. Spring Boot Static Resource Configuration**

Create `backend/src/main/java/com/hna/webserver/config/StaticResourceConfig.java`:
```java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve Next.js static files from /admin
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
```

**5. Spring Boot Route Priority**

Ensure redirect controller has higher priority than static resources:
- Redirect controller: `@GetMapping("/{slug}")` with high priority
- Static resources: Lower priority, only match `/admin/**`
- API routes: `/api/**` handled by API controllers

**6. Docker Compose for Production**

Update `docker-compose.prod.yml`:
```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile.prod
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      - db
      - redis
```

#### Alternative: Option 2 (Node.js + Java in Container)

If you need Next.js SSR features, use this approach:

**Dockerfile with Both Runtimes:**
```dockerfile
# Use base image with both Java and Node.js
FROM ubuntu:22.04

# Install Java 17
RUN apt-get update && apt-get install -y openjdk-17-jdk

# Install Node.js 22
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y nodejs

# Install Traefik (lightweight reverse proxy)
RUN curl -L https://github.com/traefik/traefik/releases/download/v2.10/traefik_v2.10_linux_amd64.tar.gz | tar -xz

# Build Next.js
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Build Spring Boot
WORKDIR /app/backend
COPY backend/ ./
RUN ./gradlew clean build -x test

# Copy Traefik config
COPY traefik.yml /etc/traefik/traefik.yml

# Start both services
CMD ["sh", "-c", "java -jar /app/backend/build/libs/*.jar & npm start --prefix /app/frontend & /traefik"]
```

**Traefik Configuration:**
```yaml
entryPoints:
  web:
    address: ":80"
  websecure:
    address: ":443"

http:
  routers:
    admin:
      rule: "PathPrefix(`/admin`)"
      service: nextjs
    api:
      rule: "PathPrefix(`/api`)"
      service: springboot
    redirects:
      rule: "PathPrefix(`/`)"
      service: springboot

  services:
    nextjs:
      loadBalancer:
        servers:
          - url: "http://localhost:3000"
    springboot:
      loadBalancer:
        servers:
          - url: "http://localhost:8080"
```

### Environment Configuration

#### Local Development
```env
# Frontend (.env.local)
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE=/api

# Backend (application-dev.yml)
spring:
  datasource:
    url: jdbc:postgresql://db:5432/app
```

#### Production
```env
# Frontend (build time)
NEXT_PUBLIC_API_URL=https://alexbbt.link
NEXT_PUBLIC_API_BASE=/api

# Backend (application-prod.yml)
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/app
  cors:
    allowed-origins: https://alexbbt.link
```

### Deployment Checklist

**For Option 1 (Recommended - Single Container):**
- [ ] Configure Next.js for static export with `/admin` basePath
- [ ] Create build script to copy Next.js output to Spring Boot resources
- [ ] Create multi-stage Dockerfile
- [ ] Configure Spring Boot static resource handler for `/admin`
- [ ] Set route priority (redirects > static > API)
- [ ] Configure SSL/TLS (at load balancer or use Spring Boot SSL)
- [ ] Build and test Docker image locally
- [ ] Configure Spring Boot production profile
- [ ] Update CORS to allow production domain
- [ ] Set up database connection pooling
- [ ] Configure Redis for production
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Test redirects at root level (`alexbbt.link/abc123`)
- [ ] Test admin interface at `/admin`
- [ ] Test API endpoints at `/api/*`

**For Option 2 (Node.js + Java):**
- [ ] Add Node.js to Dockerfile base image
- [ ] Configure Traefik/Nginx for internal routing
- [ ] Build Next.js in standalone mode
- [ ] Configure both services to run in container
- [ ] Set up process management (supervisord or similar)
- [ ] Test all routing paths
- [ ] Monitor resource usage (larger container)

### Performance Comparison

**Option 1 (Static Export):**
- Container size: ~150-200MB (Java only)
- Memory usage: ~256MB-512MB
- Redirect latency: < 5ms (direct Spring Boot)
- Admin load time: Fast (static files, cached by browser)

**Option 2 (Node.js + Java):**
- Container size: ~500-700MB (Java + Node.js)
- Memory usage: ~512MB-1GB
- Redirect latency: < 10ms (through internal proxy)
- Admin load time: Fast (with SSR capability)

**Option 3 (Reverse Proxy):**
- Infrastructure: Multiple containers/services
- Memory usage: Distributed across services
- Redirect latency: < 15ms (through external proxy)
- Admin load time: Fast (full Next.js features)
- Scalability: Best (independent scaling)

## Domain Considerations

Since the service will be hosted on `alexbbt.link`:
- Update CORS config to include production domain (`https://alexbbt.link`)
- Configure reverse proxy routing rules
- Ensure root-level routes work correctly for redirects
- SSL/HTTPS required for production
- Consider DNS propagation for domain setup

## Performance Targets

- **Redirect Response Time**: < 10ms (with cache hit)
- **Database Query Time**: < 50ms (cache miss)
- **Admin API Response**: < 100ms
- **Cache Hit Rate**: > 90% for active links

## Future Enhancements

1. **User Authentication**: Multi-user support with link ownership
2. **Advanced Analytics**: Detailed click tracking with charts
3. **Custom Domains**: Support for multiple domains
4. **QR Code Generation**: Generate QR codes for short links
5. **Link Expiration**: Automatic cleanup of expired links
6. **Bulk Import/Export**: CSV import/export for links
7. **API Rate Limiting**: Protect against abuse
8. **Link Preview**: Show preview cards when sharing links
