# 10 — V1 Completion

## Overview

This milestone marks the completion of the V1 MVP of the Event Hall Package Builder.

The application is now a fully functional full-stack system with both customer and admin flows.

---

## Features Implemented

### Customer Flow

- Select event type
- Enter guest count
- Select upgrades
- Filter upgrades by category
- View live price estimate
- Enter customer details:
    - Name
    - Email
    - Phone number
- Submit quote request
- View quote summary

---

### Admin Flow

- View all quotes
- Filter quotes by status
- Update quote status
- View all upgrades (active + inactive)
- Create new upgrades
- Edit upgrade details
- Enable/disable upgrades (soft delete)
- Filter upgrades by category and status

---

## Backend

- REST API built with Spring Boot
- Layered architecture:
    - Controller
    - Service
    - Repository
- DTO-based API design
- Validation:
    - Required fields
    - Email format
    - Phone number format
- Price calculation handled in backend
- Soft delete for upgrades
- Filtering support

---

## Frontend

- React + Vite + TypeScript
- API integration using Axios
- Routing with React Router
- State management using React hooks
- Category and status filtering
- Global UI styling system

---

## Testing & CI

- Integration tests using MockMvc
- Validation tests
- H2 in-memory database for testing
- GitHub Actions CI pipeline

---

## Result

At the end of V1:

- Full customer journey is implemented
- Admin panel supports real business workflows
- Application is stable and testable
- Codebase follows clean architecture principles

---

## Next Steps (V1.5)

- UI/UX improvements
- Search and sorting
- Dashboard statistics
- Better error handling
- Deployment