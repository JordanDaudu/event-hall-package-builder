# Event Hall Package Builder

A full-stack portfolio project that allows customers to build event hall packages, submit quote requests, and lets admins manage quotes and upgrade options.

---

## Overview

Event Hall Package Builder is a full-stack web application built to practice professional backend and frontend development.

Customers can select an event type, enter a guest count, choose upgrades, see a live price preview, and submit a quote request.

Admins can view submitted quotes, filter quotes by status, update quote statuses, and manage available upgrades.

---

## Features

### Customer Features

- Select event type
- Enter guest count
- Filter upgrades by category
- Select package upgrades
- View live price preview
- Enter customer details:
  - Name
  - Email
  - Phone number
- Submit quote request
- View quote summary page

### Admin Features

- View all submitted quotes
- Filter quotes by status
- Update quote status:
  - NEW
  - CONTACTED
  - APPROVED
  - REJECTED
- View all upgrades, including inactive upgrades
- Create new upgrades
- Edit upgrade details:
  - Name
  - Description
  - Category
  - Price
- Enable or disable upgrades using soft delete
- Filter upgrades by category and active status

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Jakarta Validation
- Swagger UI / OpenAPI
- Maven

### Frontend

- React
- Vite
- TypeScript
- Axios
- React Router

### Testing and CI

- JUnit
- MockMvc
- H2 test database
- GitHub Actions

---

## Architecture

```text
React Frontend
    ↓ REST API calls
Spring Boot Backend
    ↓ Spring Data JPA
PostgreSQL Database
```

Backend structure follows a layered architecture:

```text
controller
service
repository
entity
dto
enums
exception
mapper
```

---

## Screenshots

Screenshots should be placed in:

```text
assets/screenshots/
```

### Package Builder

![Package Builder](./assets/screenshots/package-builder.png)

### Quote Summary

![Quote Summary](./assets/screenshots/quote-summary.png)

### Admin Quotes

![Admin Quotes](./assets/screenshots/admin-quotes.png)

### Admin Upgrades

![Admin Upgrades](./assets/screenshots/admin-upgrades.png)

---

## API Documentation

When the backend is running, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

---

## Main API Endpoints

### Public Endpoints

```text
GET  /api/event-types
GET  /api/upgrades
POST /api/quotes
GET  /api/quotes/{id}
```

### Admin Quote Endpoints

```text
GET /api/admin/quotes
GET /api/admin/quotes?status=NEW
PUT /api/admin/quotes/{id}/status
```

### Admin Upgrade Endpoints

```text
GET    /api/admin/upgrades
POST   /api/admin/upgrades
PUT    /api/admin/upgrades/{id}
DELETE /api/admin/upgrades/{id}
```

---

## Pricing Rule

The frontend shows a live price preview for user experience.

The backend calculates the final price for security.

```text
Total Price = (Event Type Base Price × Guest Count) + Selected Upgrades Total
```

---

## Getting Started

### Prerequisites

- Java 21
- Node.js
- npm
- PostgreSQL

---

## Backend Setup

Create a PostgreSQL database:

```text
eventhall_db
```

Set environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eventhall_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
```

Run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Frontend Setup

Go into the frontend folder:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Run the frontend:

```bash
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## Running Tests

Run backend tests:

```bash
cd backend
./mvnw test
```

The test profile uses an H2 in-memory database, so tests do not require PostgreSQL.

---

## CI

GitHub Actions runs tests automatically on push and pull request.

---

## Important Notes

- Backend is the source of truth for price calculation.
- Admin endpoints do not require authentication in V1.
- Authentication will be added in a later version.
- Upgrade deletion is implemented as soft delete using the `active` field.
- Inactive upgrades are hidden from customers but visible to admins.

---

## V1 Status

V1 MVP is complete.

Completed:

- Spring Boot backend
- PostgreSQL database integration
- React frontend
- Customer quote flow
- Admin quote management
- Admin upgrade management
- API validation
- Testing and CI
- Basic UI polish

---

## Future Improvements

### V1.5

- Better UI and responsive design
- Dashboard statistics
- Search and sorting
- Better error handling
- Docker Compose
- Deployment

### V2

- Authentication
- Admin login
- Customer accounts
- Email notifications
- File/image uploads for upgrades

### V3

- React Native / Expo mobile version
- Same Spring Boot backend
- Same PostgreSQL database
