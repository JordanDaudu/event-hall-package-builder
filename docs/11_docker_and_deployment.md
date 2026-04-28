# 11 — Docker and Deployment

## Overview

In this stage, the application was containerized using Docker.

This allows the full stack to run consistently across different machines without requiring manual installation of Java, Node, PostgreSQL, or build tools.

This stage includes:

- Dockerizing the Spring Boot backend
- Dockerizing the React frontend
- Running PostgreSQL in Docker
- Using Docker Compose to run the full stack
- Publishing private Docker images with GitHub Actions
- Supporting both ARM64 and AMD64 platforms

---

## 1. Project Structure

The project was organized into a full-stack structure:

    backend/
    frontend/
    docker-compose.yml
    docker-compose.images.yml
    documentation/
    developer-guides/
    assets/

This separates backend and frontend responsibilities and makes Docker setup cleaner.

---

## 2. Backend Dockerfile

The backend uses a multi-stage Docker build.

### Build stage

- Uses JDK image
- Copies Maven wrapper and source code
- Builds the Spring Boot JAR

### Runtime stage

- Uses lighter JRE image
- Copies only the built JAR
- Runs the application with Java

### Benefit

The final backend image is smaller and does not contain unnecessary build tools.

---

## 3. Frontend Dockerfile

The frontend also uses a multi-stage Docker build.

### Build stage

- Uses Node image
- Installs dependencies with `npm ci`
- Builds the React application

### Runtime stage

- Uses Nginx image
- Serves the generated static files from the `dist` folder

### Benefit

The final frontend image only contains static production files and Nginx.

---

## 4. Docker Compose for Local Development

File:

    docker-compose.yml

This file builds images from local source code.

### Services

- postgres
- backend
- frontend

### Purpose

Used when developing locally and building containers from the current codebase.

### Command

    docker compose up --build

---

## 5. Docker Compose for Published Images

File:

    docker-compose.images.yml

This file does not build from source code.

Instead, it pulls pre-built images from GitHub Container Registry.

### Purpose

Used for running the application on another machine without building locally.

### Command

    docker compose -f docker-compose.images.yml up

---

## 6. PostgreSQL in Docker

PostgreSQL runs as a Docker container using:

    postgres:16

The backend connects to it using the Compose service name:

    jdbc:postgresql://postgres:5432/eventhall_db

Inside Docker, the backend should not use `localhost` for PostgreSQL because `localhost` would refer to the backend container itself.

---

## 7. Environment Variables

The backend receives database configuration through environment variables:

    SPRING_DATASOURCE_URL
    SPRING_DATASOURCE_USERNAME
    SPRING_DATASOURCE_PASSWORD

This keeps configuration outside the source code and prepares the project for deployment.

---

## 8. Persistent Database Volume

PostgreSQL uses a Docker volume:

    eventhall_postgres_data

This allows data to persist even if containers are stopped and restarted.

To remove the database volume and reset the data:

    docker compose down -v

---

## 9. GitHub Actions Docker Publishing

A GitHub Actions workflow was added to build and publish Docker images automatically.

Workflow file:

    .github/workflows/docker-publish.yml

### Pipeline flow

1. Push code to GitHub
2. Run backend tests
3. Build frontend
4. Build backend Docker image
5. Build frontend Docker image
6. Push images to GitHub Container Registry

---

## 10. GitHub Container Registry

Docker images are stored in GitHub Container Registry.

Images:

    ghcr.io/jordandaudu/eventhall-backend
    ghcr.io/jordandaudu/eventhall-frontend

The images are private and require authentication before pulling.

---

## 11. Multi-Architecture Builds

The GitHub Actions workflow builds images for:

    linux/amd64
    linux/arm64

### Why this matters

- `linux/arm64` supports Apple Silicon Macs
- `linux/amd64` supports most Windows/Linux PCs and servers

This makes the images portable across different machines.

---

## 12. Docker Ignore Files

Docker ignore files were added:

    backend/.dockerignore
    frontend/.dockerignore

### Purpose

- Keep Docker build context small
- Avoid copying unnecessary files
- Prevent local files from entering images

Examples of ignored files:

- node_modules
- dist
- target
- .env
- IDE folders

---

## 13. Running the Full Stack

From the project root, run:

    docker compose up --build

Frontend:

    http://localhost:5173

Backend:

    http://localhost:8080

Swagger UI:

    http://localhost:8080/swagger-ui.html

---

## 14. Running on Another Computer

On another computer, use:

    docker compose -f docker-compose.images.yml pull
    docker compose -f docker-compose.images.yml up

This pulls private images from GHCR and starts the application without needing source code or build tools.

---

## Result

At the end of this stage:

- The full stack is Dockerized
- PostgreSQL runs in Docker
- Backend and frontend run in containers
- GitHub Actions publishes private images
- Images support both AMD64 and ARM64
- The application can run on another machine using Docker only

---

## Next Steps

- Improve admin UI
- Improve production configuration
- Add authentication for admin routes
- Add deployment target such as VPS or cloud hosting
