# URL Shortener

A fast, performant URL shortener service built with Spring Boot and Next.js. Create short links with custom or random slugs, track click statistics, and manage everything through a modern admin interface.

**Single-container production deployment** - Next.js frontend is built as static files and embedded in the Spring Boot JAR for simple deployment.

**Zero-install development setup** - Get started instantly with Docker containers for all services and dependencies. No need to install Java, Gradle, or databases locally.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Initial Setup](#initial-setup)
- [Quick Start](#quick-start)
- [Development Commands](#development-commands)
- [Project Structure](#project-structure)
- [API Integration](#api-integration)
- [Development Notes](#development-notes)
- [CI/CD Pipeline](#cicd-pipeline)
- [Testing Production Build Locally](#testing-production-build-locally)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Features

- **Fast Redirects** - Sub-10ms redirect lookups using Redis caching
- **Custom & Random Slugs** - Create short links with custom slugs or auto-generated 6-character codes
- **Admin Interface** - React-based admin panel at `/admin` for managing links and viewing statistics
- **Root-Level Routing** - Short links work at domain root (e.g., `alexbbt.link/abc123`)
- **Click Tracking** - Automatic click counting and analytics
- **Production Ready** - Single-container deployment with Next.js static files embedded in Spring Boot
- **High Performance** - Redis cache for active links, PostgreSQL for persistence
- **Type Safe** - Full TypeScript interfaces for API responses
- **Developer Friendly** - Hot reload for both frontend and backend during development

## Prerequisites

- **Docker Desktop** (macOS/Windows) or **Docker Engine** (Linux)
- **Node.js 22.20.0** (see `.nvmrc` file for exact version)
- **Terminal access** for running commands
- **(Windows users)** Git Bash or WSL recommended for best experience

**Note**: No Java or Gradle installation needed - backend runs in Docker containers.

  <details>
  <summary><strong>🏫 School Network Users - Important Note</strong></summary>

  If you're on a school network with security restrictions, you may need to:
  - Use a specific NVM mirror for Node.js installation
  - Temporarily disable SSL verification for npm installs

  See the [Initial Setup](#initial-setup) section for detailed instructions.

  </details>

## Initial Setup

### 1. Install Required Software

**Docker Desktop**
- Download from: https://www.docker.com/products/docker-desktop/
- Install and start Docker Desktop (takes a moment to boot)

**Visual Studio Code**
- Download from: https://code.visualstudio.com/download
- Install and verify it works

**Git**
- **Windows users**: Download from https://gitforwindows.org/ (includes Git Bash)
- **Mac users**: Install Xcode Command Line Tools: `xcode-select --install`

  <details>
  <summary><strong>🍎 Mac Users - Xcode Command Line Tools</strong></summary>

  If you don't have Git installed on Mac, run this command in Terminal:

  ```bash
  xcode-select --install
  ```

  This will install Git and other essential development tools. You may be prompted to install additional software - click "Install" when prompted.

  </details>

### 2. Clone the Repository

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd alexbbt.link
   ```
2. Open the project in your IDE

### 3. Fix Line Endings (Windows Users Only)

  <details>
  <summary><strong>🪟 Windows Users - Line Endings Fix</strong></summary>

  To ensure all files use Linux (LF) line endings (required for Docker and shell scripts), run this command in Git Bash:

  ```bash
  find . -type f -not -path '*/\.git/*' -exec dos2unix {} +; git checkout .
  ```

  This converts Windows (CRLF) line endings to Unix (LF) format, which is required for proper Docker container execution.

  </details>

### 4. Install Node Version Manager (NVM)

**Install NVM:**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
```

**Configure NVM:**
- **Windows (Git Bash):** `nano ~/.bash_profile`
- **Mac:** `nano ~/.zshrc`

Add this content:
```bash
export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm
```

Save with: `Ctrl+X`, then `Y`, then `Enter`

**Open a new terminal** (Git Bash on Windows)

### 5. Install Node.js

  <details>
  <summary><strong>🏫 School Network (with security restrictions)</strong></summary>

  ```bash
  NVM_NODEJS_ORG_MIRROR=http://nodejs.org/dist nvm install
  ```

  </details>

  <details>
  <summary><strong>🏠 Home Network</strong></summary>

  ```bash
  nvm install
  ```

  </details>

### 6. Install Project Dependencies

  <details>
  <summary><strong>🏫 School Network (with security restrictions)</strong></summary>

  ```bash
  cd frontend
  # Disable SSL verification temporarily for school network
  npm config set strict-ssl false
  npm install
  # Re-enable SSL verification for security
  npm config set strict-ssl true
  ```

  </details>

  <details>
  <summary><strong>🏠 Home Network</strong></summary>

  ```bash
  cd frontend
  npm install
  ```

  </details>

### 7. Verify Your Setup

After completing all setup steps, verify everything is working:

```bash
# Check if Docker is running
docker --version

# Check if Node.js is installed
node --version  # Should show v22.20.0

# Check if Git is working
git --version

# Test the sail script
./sail --version
```

If any of these commands fail, go back to the relevant setup step above.

## Quick Start

> **First time?** Make sure you've completed the [Initial Setup](#initial-setup) steps above.

1. **Start the development environment:**
   ```bash
   ./sail up
   ```

2. **Run the backend:**
   ```bash
   ./sail backend
   ```

3. **Run the frontend (in a new terminal):**
   ```bash
   ./sail frontend
   ```

4. **Verify everything is working:**
   - Open http://localhost:3000/admin - you should see the admin interface
   - Open http://localhost:8080/api/health - you should see `{"status":"UP"}`

## 🌐 Development URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Admin Interface** | http://localhost:3000/admin | URL shortener admin panel |
| **Backend API** | http://localhost:8080/api | REST API endpoints |
| **Short Links** | http://localhost:8080/{slug} | Redirect to original URL |
| **Mailpit UI** | http://localhost:8025 | Email testing interface (SMTP: 1025) |
| **PostgreSQL** | localhost:5432 | Database (user: `app`, pass: `app`, db: `app`) |
| **Redis** | localhost:6379 | Cache for active short links |

## Development Commands

### Full-Stack Development
```text
./sail up        Start dev environment (services only)
./sail down      Stop everything
./sail restart   Restart all containers
```

### Backend Commands
```text
./sail run       Run the backend (hot reload via devtools)
./sail backend   Run the backend (alias for run)
./sail debug     Run the backend with debug port 5005 enabled
./sail test      Run backend tests in the container
./sail clean     Clean build artifacts in the container
./sail backend:lint Run backend checkstyle and SpotBugs linting
```

### Frontend Commands
```text
./sail frontend       Run the frontend dev server
./sail frontend:dev   Run the frontend dev server (alias)
./sail frontend:build Build the frontend for production
./sail frontend:start Start the frontend production server
./sail frontend:lint  Run frontend linting
```

### Docker Commands
```text
./sail build     Rebuild the app image
./sail sh        Shell into the app container (bash)
./sail logs      Tail all container logs
./sail ps        Show container status
./sail status    Check Docker daemon and container status
./sail psql      Open psql in the Postgres container
./sail redis-cli Open redis-cli in the Redis container
```

## 🚀 Common Workflows

### Daily Development
```bash
# Start your day
./sail up              # Start all services
./sail backend         # Run backend (Terminal 1)
./sail frontend        # Run frontend (Terminal 2)
```

### Making Changes
```bash
# Backend changes - automatic restart via DevTools
# Frontend changes - automatic hot reload

# Test your changes
./sail test            # Run backend tests
./sail frontend:lint   # Check frontend code quality
```

### Debugging Issues
```bash
./sail ps              # Check what's running
./sail logs            # View all logs
./sail status          # Check Docker status
```

### Stopping Everything
```bash
./sail down            # Stop all containers
```

## Project Structure

```
├── backend/                    # Spring Boot backend
│   ├── src/main/java/         # Java source code
│   │   └── com/hna/webserver/
│   │       ├── config/        # CORS, Redis, Static resources
│   │       ├── controller/    # RedirectController, ShortLinkController, HealthController
│   │       ├── model/         # ShortLink entity
│   │       ├── repository/   # ShortLinkRepository
│   │       ├── service/       # ShortLinkService with caching
│   │       ├── dto/           # Request/Response DTOs
│   │       └── util/          # SlugGenerator, UrlValidator
│   ├── src/main/resources/    # Application configuration
│   │   ├── application.properties
│   │   └── application-dev.yml # Docker dev profile
│   ├── build.gradle           # Gradle build configuration
│   └── config/checkstyle/     # Code quality configuration
├── frontend/                  # Next.js frontend
│   ├── src/app/              # App router pages
│   │   ├── admin/            # Admin interface for URL shortener
│   │   │   └── page.tsx      # Admin dashboard
│   │   └── page.tsx          # Home page (redirects to /admin)
│   ├── src/components/admin/ # Admin UI components
│   │   ├── CreateLinkForm.tsx
│   │   ├── LinkList.tsx
│   │   └── LinkStats.tsx
│   ├── src/lib/              # API utilities
│   │   └── api.ts            # Centralized API client
│   ├── package.json          # Frontend dependencies
│   ├── next.config.ts        # Next.js configuration
│   └── tsconfig.json         # TypeScript configuration
├── .github/workflows/         # GitHub Actions CI/CD workflows
│   ├── ci.yml                # Main CI pipeline
│   ├── backend-*.yml         # Backend-specific workflows
│   └── frontend-*.yml        # Frontend-specific workflows
├── docker-compose.yml         # Development Docker services
├── docker-compose.prod.yml    # Production Docker services
├── Dockerfile.dev             # Development container image
├── Dockerfile.prod            # Production container image (multi-stage)
├── scripts/                   # Build scripts
│   └── build-production.sh    # Production build script
├── .nvmrc                     # Node.js version specification
├── sail                       # Command helper script
├── DEPLOYMENT.md              # Deployment guide
└── README.md                  # This file
```

## API Integration

### URL Shortener Endpoints
The backend provides the following REST endpoints:
- `GET /api/health` - Health check
- `POST /api/shortlinks` - Create a short link
- `GET /api/shortlinks` - List all short links (paginated)
- `GET /api/shortlinks/{slug}` - Get a short link by slug
- `DELETE /api/shortlinks/{slug}` - Delete a short link
- `GET /api/shortlinks/stats` - Get statistics
- `GET /{slug}` - Redirect to original URL (root-level)

### Using the API
```typescript
import { api } from '@/lib/api';

// Create a short link
const link = await api.shortlinks.create({ url: 'https://example.com' });

// Get all links
const links = await api.shortlinks.list();

// Get statistics
const stats = await api.shortlinks.stats();
```

## Development Notes

- **Hot reload**: Spring Boot DevTools for backend, Next.js hot reload for frontend
- **CORS**: Configured to allow frontend requests from localhost:3000
- **API Proxying**: Next.js automatically proxies `/api/*` to backend
- **Type Safety**: TypeScript interfaces for all API responses
- **Profiles**: Backend runs with `SPRING_PROFILES_ACTIVE=dev` in Docker
- **Database**: PostgreSQL stores all short links with automatic schema creation
- **Caching**: Redis caches active links for 24 hours for fast redirects
- **Email**: Mailpit available for email testing (if needed in future)

### Development Workflow

1. **Start services**: `./sail up` (starts Docker containers)
2. **Run backend**: `./sail backend` (in one terminal)
3. **Run frontend**: `./sail frontend` (in another terminal)
4. **Make changes**: Edit code in your IDE
5. **See changes**: Frontend updates automatically, backend restarts automatically
6. **Test**: Visit `http://localhost:3000/admin` to create short links

## CI/CD Pipeline

The `.github/workflows/` folder contains GitHub Actions workflows that automatically run on every push and pull request, handling linting, building, and testing for both frontend and backend.

## Testing Production Build Locally

Test the production Docker image locally before deploying:

### Build and Run Production Stack

```bash
# Build and start all services (app, database, redis)
docker compose -f docker-compose.prod.yml up --build

# Or run in detached mode
docker compose -f docker-compose.prod.yml up --build -d
```

This will:
1. Build the Next.js frontend as static files
2. Package frontend files into Spring Boot JAR
3. Build the production Docker image
4. Start PostgreSQL, Redis, and the application

### Access the Application

Once running, access:
- **Admin Interface**: http://localhost:8080/admin
- **API Health Check**: http://localhost:8080/api/health
- **Short Links**: http://localhost:8080/{slug}

### View Logs

```bash
# View all logs
docker-compose -f docker-compose.prod.yml logs -f

# View app logs only
docker-compose -f docker-compose.prod.yml logs -f app
```

### Stop Services

```bash
# Stop and remove containers
docker-compose -f docker-compose.prod.yml down

# Stop and remove containers + volumes (clears database)
docker-compose -f docker-compose.prod.yml down -v
```

### Environment Variables

You can override the base URL for testing:

```bash
BASE_URL=http://localhost:8080 docker-compose -f docker-compose.prod.yml up --build
```

## Deployment

The application is designed for production deployment with a single Docker container. The Next.js frontend is built as static files and embedded in the Spring Boot JAR, eliminating the need for a separate frontend server or reverse proxy.

### Production Features

- **Single Container** - One Docker image contains everything (backend + frontend)
- **No Node.js Runtime** - Only Java/JVM needed in production
- **Fast Redirects** - Direct Spring Boot routing for root-level short links
- **Static Admin UI** - Admin interface served as static files from Spring Boot

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions and production configuration.

## Troubleshooting

### Common Issues

  <details>
  <summary><strong>🌐 CORS Errors</strong></summary>

  - Ensure backend is running on port 8080
  - Check CORS configuration in `backend/src/main/java/com/hna/webserver/config/CorsConfig.java`
  - Verify frontend is running on localhost:3000

  </details>

  <details>
  <summary><strong>🔌 API Connection Failed</strong></summary>

  - Verify backend is running: `./sail ps`
  - Check backend logs: `./sail logs`
  - Ensure services are up: `./sail up`

  </details>

  <details>
  <summary><strong>⚛️ Frontend Not Loading</strong></summary>

  - Ensure Node.js version matches `.nvmrc` (22.20.0)
  - Install dependencies: `cd frontend && npm install`
  - Check if port 3000 is available

  </details>

  <details>
  <summary><strong>🗄️ Database Connection Issues</strong></summary>

  - Ensure PostgreSQL is running: `./sail ps`
  - Check database logs: `./sail logs db`
  - Verify connection settings in `application-dev.yml`
  - Database schema is created automatically on first run

  </details>

  <details>
  <summary><strong>🔗 Short Link Redirects Not Working</strong></summary>

  - Verify the slug exists: Check admin interface or database
  - Check Redis is running: `./sail ps` (should see redis container)
  - Verify redirect controller is working: `curl http://localhost:8080/api/health`
  - Check application logs for errors: `./sail logs`

  </details>

  <details>
  <summary><strong>📊 Admin Interface Not Loading</strong></summary>

  - Ensure you're accessing `/admin` (not root)
  - In development: Visit http://localhost:3000/admin
  - In production: Visit http://localhost:8080/admin
  - Check browser console for errors
  - Verify API is accessible: `curl http://localhost:8080/api/health`

  </details>

  <details>
  <summary><strong>🐳 Docker Issues</strong></summary>

  - Check Docker daemon is running: `./sail status`
  - Restart Docker Desktop if needed
  - Clean up containers: `./sail down && docker system prune`
  - Rebuild containers: `./sail build`

  </details>

  <details>
  <summary><strong>🔌 Port Conflicts</strong></summary>

  - Ensure ports 3000, 8080, 5432, 6379, and 8025 are available
  - Check what's using a port: `lsof -i :PORT_NUMBER` (macOS/Linux)
  - Kill process using port: `kill -9 PID` (replace PID with actual process ID)

  </details>

  <details>
  <summary><strong>🪟 Windows Terminal Issues</strong></summary>

  - Always use Git Bash instead of PowerShell for running `./sail` commands
  - In VSCode, click the arrow next to the `+` button in terminal and select "Git Bash"
  - If commands don't work, ensure you're in the project root directory

  </details>

### Debug Commands
```bash
# Check service status
./sail ps

# View all logs
./sail logs

# Check Docker status
./sail status

# Shell into backend container
./sail sh

# Test API endpoints directly
curl http://localhost:8080/api/health
curl http://localhost:8080/api/shortlinks/stats
```

### Getting Help

If you're still having issues:

1. **Check the logs**: `./sail logs` - look for error messages
2. **Restart everything**: `./sail restart`
3. **Verify your setup**: Make sure you completed all [Initial Setup](#initial-setup) steps
4. **Check your environment**: Ensure Docker is running and ports are available
5. **Ask for help**: Contact your instructor or team members

### Quick Health Check

Run this command to verify everything is working:

```bash
# Check all services
./sail ps

# Test API connectivity
curl http://localhost:8080/api/health

# Check frontend
open http://localhost:3000
```
