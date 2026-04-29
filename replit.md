# Event Hall Package Builder

A full-stack web application for event hall businesses, allowing customers to build custom event packages and request quotes. Administrators can manage quotes, upgrades, and view business analytics.

## Architecture

- **Frontend**: React 19 + TypeScript + Vite, running on port 5000
- **Backend**: Spring Boot 4.0.6 (Java 19) REST API, running on port 8080
- **Database**: PostgreSQL (Replit built-in)

## Project Structure

```
├── frontend/          # React/Vite frontend (port 5000)
│   ├── src/
│   │   ├── api/       # Axios API client modules
│   │   ├── components/# Reusable React components
│   │   ├── pages/     # Page components
│   │   └── types/     # TypeScript interfaces
│   └── vite.config.ts # Vite config (proxy /api -> localhost:8080)
├── backend/           # Spring Boot backend (port 8080)
│   └── src/main/java/com/eventhall/
│       ├── config/    # CORS, business config
│       ├── controller/# REST API endpoints
│       ├── service/   # Business logic
│       ├── repository/# JPA repositories
│       ├── entity/    # JPA entities
│       ├── dto/       # Data transfer objects
│       └── mapper/    # Entity <-> DTO mappers
└── docker-compose.yml # Docker setup (not used in Replit)
```

## Workflows

- **Start application**: `cd frontend && npm run dev` (port 5000, webview)
- **Start Backend**: `cd backend && ./mvnw spring-boot:run` (port 8080, console)

## Environment Variables

- `SPRING_DATASOURCE_URL`: PostgreSQL JDBC URL (set from Replit database)
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `VITE_API_URL`: API base URL (optional, defaults to /api via Vite proxy)

## API Routing

The frontend Vite dev server proxies all `/api/*` requests to `http://localhost:8080`. The API client defaults to `/api` for relative URL usage.

## Branding & Design

- **Adama logo** (client) — used as the main navbar brand: `frontend/src/assets/logos/adama-logo.jpeg`
- **JD logo** (developer) — small footer credit ("Crafted by JD"), dark-mode variant: `frontend/src/assets/logos/jd-logo.png`
- **Design system** — elegant warm-cream/charcoal palette, Cormorant Garamond serif headings + Inter body, defined via CSS variables in `frontend/src/styles/global.css`.

## Authentication (Phase 2 — added)

The backend now uses **Spring Security + JWT (HS256)** for authentication.

- **Roles**: `ADMIN`, `CUSTOMER` (no public registration; admin creates customers).
- **Entity**: `UserAccount` (full name, email, BCrypt password hash, role, active flag, customer identity number, base package price).
- **Endpoints**: `POST /api/auth/login`, `GET /api/auth/me` (returns current user without password hash).
- **Filter**: `JwtAuthenticationFilter` reads `Authorization: Bearer <token>` and populates the security context.
- **Stateless** session, CSRF disabled (REST only), CORS handled centrally in `SecurityConfig`.
- **Authorization map** (current):
  - `POST /api/auth/login` → public
  - `/api/auth/**` → authenticated
  - `/api/admin/**` → `ROLE_ADMIN`
  - `/api/customer/**` → `ROLE_CUSTOMER`
  - Legacy public endpoints (`/api/event-types`, `/api/upgrades`, `/api/quotes`, `/api/config`) remain `permitAll` during the migration; they will be moved or removed as the new domain lands.
  - Swagger UI / OpenAPI remain open for development.
- **Default admin** is seeded at startup if none exists. Override with env vars:
  - `ADMIN_EMAIL` (default `admin@adama.local`)
  - `ADMIN_PASSWORD` (default `admin1234` — dev only)
  - `ADMIN_FULL_NAME` (default `מנהל המערכת`)
- **JWT settings** (env vars):
  - `JWT_SECRET` (must be ≥ 32 bytes / 256 bits for HS256; dev default included in `application.properties`)
  - `JWT_EXPIRATION_MINUTES` (default `480`)
  - `JWT_ISSUER` (default `adama-event-hall`)

## Venues (Phase 4 — added)

`Venue` entity with Hebrew/English names, Hebrew description, image URL, active flag, and sort order.

- **Public endpoint**: `GET /api/venues` — returns only active venues, sorted by `sortOrder`. No auth required; venue names/descriptions are not sensitive.
- **Admin endpoints** (`/api/admin/venues`): list all (including inactive), create, update, toggle active, soft-delete. All require ADMIN role.
- **Soft-delete convention**: `DELETE /api/admin/venues/{id}` sets `active=false` rather than removing the row. This preserves consistency for future package requests that will reference venues.
- **Seeder**: Two default venues are created at startup if none exist — "אולם הגן" (Garden Hall) and "אולם הכוכבים" (Stars Hall).
- `VenueService.requireActiveVenue(id)` is a helper pre-built for Phase 7 (package request submission) to validate venue selection.

## Key Notes

- Java version set to 19 in pom.xml (Spring Boot 4 supports Java 19+)
- CORS configured centrally in `SecurityConfig#corsConfigurationSource` (allows all origins; tighten for production)
- Database schema auto-created by Hibernate on startup; default admin user seeded if no admin exists
- Swagger UI available at `/swagger-ui.html` on the backend (port 8080)
