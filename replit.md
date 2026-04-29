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

## Key Notes

- Java version set to 19 in pom.xml (Spring Boot 4 supports Java 19+)
- CORS configured to allow all origins (`allowedOriginPatterns("*")`)
- Database schema auto-created by Hibernate on startup with seed data (event types and upgrades)
- Swagger UI available at `/swagger-ui.html` on the backend (port 8080)
