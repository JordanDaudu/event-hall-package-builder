# Run Event Hall Package Builder on Another Computer

This guide explains how to run the application on another machine using Docker and pre-built private images from GitHub Container Registry (GHCR).

---

## Prerequisites

- Docker Desktop installed and running
- Internet connection
- GitHub account with access to the private packages
- A GitHub personal access token with `read:packages`

---

## 1. Get the Compose File

Copy this file to the target machine:

    docker-compose.images.yml

Place it in any folder, for example:

    eventhall-run/

You do not need the full source code if you are using the published Docker images.

---

## 2. Create a GitHub Token

Go to GitHub:

    Settings -> Developer settings -> Personal access tokens -> Tokens (classic)

Create a token with only this scope:

    read:packages

Copy the token immediately after creation.

---

## 3. Login to GitHub Container Registry

Open a terminal in the folder that contains `docker-compose.images.yml`.

Run:

    docker login ghcr.io

Use:

    Username: your GitHub username
    Password: your GitHub token

Example username:

    JordanDaudu

---

## 4. Pull the Images

Run:

    docker compose -f docker-compose.images.yml pull

This downloads:

- PostgreSQL image from Docker Hub
- Backend image from GHCR
- Frontend image from GHCR

---

## 5. Run the Application

Run:

    docker compose -f docker-compose.images.yml up

Or run in the background:

    docker compose -f docker-compose.images.yml up -d

---

## 6. Access the Application

Frontend:

    http://localhost:5173

Backend API:

    http://localhost:8080

Swagger UI:

    http://localhost:8080/swagger-ui.html

---

## 7. Stop the Application

Run:

    docker compose -f docker-compose.images.yml down

---

## 8. Reset the Database

To stop the app and delete the PostgreSQL volume:

    docker compose -f docker-compose.images.yml down -v

Use this only when you want a fresh database.

---

## 9. Update to the Latest Version

When new images are pushed to GHCR, update the local machine with:

    docker compose -f docker-compose.images.yml pull
    docker compose -f docker-compose.images.yml up -d

---

## Notes

- The Docker images are private.
- The app requires GHCR authentication before pulling images.
- The target machine does not need Java, Node, Maven, or PostgreSQL installed locally.
- Docker Compose starts PostgreSQL, backend, and frontend together.
