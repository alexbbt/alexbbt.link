# Spring Boot + Next.js Integration Guide

This project demonstrates how to integrate Spring Boot (backend) with Next.js (frontend) for a seamless full-stack development experience.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.5.6 with Java 17
- **Frontend**: Next.js 15.5.4 with React 19
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Development**: Docker Compose + Sail script

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Node.js 22.20.0 (see `.nvmrc`)
- Java 17+ (for local development)

### Start Everything
```bash
# Start all services (database, redis, mail)
./sail up

# Run both frontend and backend
./sail both
```

### Individual Services
```bash
# Backend only
./sail backend

# Frontend only
./sail frontend

# Build frontend for production
./sail frontend:build
```

## 🔧 Configuration

### CORS Setup
The Spring Boot backend is configured to accept requests from:
- `http://localhost:3000` (Next.js dev server)
- `http://127.0.0.1:3000`
- `http://localhost:3001` (alternative port)

### API Configuration
- **Backend API**: `http://localhost:8080/api`
- **Frontend**: `http://localhost:3000`
- **API Proxy**: Next.js automatically proxies `/api/*` to the backend

### Environment Variables
Frontend environment variables (in `frontend/.env.local`):
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE=/api
```

## 📡 API Integration

### Available Endpoints
- `GET /api/health` - Health check
- `POST /api/shortlinks` - Create a short link
- `GET /api/shortlinks` - List all short links (paginated)
- `GET /api/shortlinks/{slug}` - Get a short link by slug
- `DELETE /api/shortlinks/{slug}` - Delete a short link
- `GET /api/shortlinks/stats` - Get statistics
- `GET /{slug}` - Redirect to original URL (root-level)

### Using the API in Next.js
```typescript
import { api } from '@/lib/api';

// Create a short link
const link = await api.shortlinks.create({ url: 'https://example.com' });

// Get all links
const links = await api.shortlinks.list();

// Get statistics
const stats = await api.shortlinks.stats();
```

## 🛠️ Development Workflow

### 1. Start Development Environment
```bash
# Start all services
./sail up

# Run both frontend and backend
./sail both
```

### 2. Development URLs
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Admin Interface**: http://localhost:3000/admin
- **Database**: localhost:5432
- **Redis**: localhost:6379
- **Mail UI**: http://localhost:8025

### 3. Hot Reload
- **Frontend**: Automatic hot reload with Next.js
- **Backend**: Automatic restart with Spring Boot DevTools

### 4. Debugging
```bash
# Debug backend
./sail debug

# Shell into backend container
./sail sh

# View logs
./sail logs
```

## 📁 Project Structure

```
├── backend/                 # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/hna/webserver/
│   │       ├── config/      # CORS configuration
│   │       └── controller/  # REST controllers
│   └── build.gradle
├── frontend/               # Next.js frontend
│   ├── src/app/           # App router pages
│   ├── lib/               # API utilities
│   ├── package.json
│   └── next.config.ts     # Next.js configuration
├── docker-compose.yml     # Services configuration
└── sail                   # Development script
```

## 🔍 Key Integration Features

### 1. CORS Configuration
- Spring Boot `CorsConfig.java` allows frontend requests
- Supports credentials and all HTTP methods
- Configured for development ports

### 2. API Proxying
- Next.js rewrites `/api/*` to backend
- Automatic CORS handling
- Environment-based configuration

### 3. Type Safety
- TypeScript interfaces for API responses
- Centralized API client in `lib/api.ts`
- Error handling utilities

### 4. Development Experience
- Unified `sail` script for all commands
- Hot reload for both frontend and backend
- Docker-based backend with local frontend
- Comprehensive logging and debugging

## 🚀 Production Deployment

### Frontend Build
```bash
./sail frontend:build
```

### Backend Build
```bash
./sail clean
./sail build
```

### Environment Variables
Update `NEXT_PUBLIC_API_URL` to point to your production backend URL.

## 🐛 Troubleshooting

### Common Issues

1. **CORS Errors**
   - Ensure backend is running on port 8080
   - Check CORS configuration in `CorsConfig.java`

2. **API Connection Failed**
   - Verify backend is running: `./sail ps`
   - Check backend logs: `./sail logs`

3. **Frontend Not Loading**
   - Ensure Node.js version matches `.nvmrc`
   - Install dependencies: `cd frontend && npm install`

4. **Database Connection Issues**
   - Ensure PostgreSQL is running: `./sail ps`
   - Check database logs: `./sail logs db`

### Debug Commands
```bash
# Check service status
./sail ps

# View all logs
./sail logs

# Shell into backend
./sail sh

# Check Docker status
./sail status
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Next.js Documentation](https://nextjs.org/docs)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [CORS Configuration Guide](https://spring.io/guides/gs/rest-service-cors/)
