# 08 — Frontend: Initial Customer Flow

## Overview

In this stage, we introduced the frontend application and connected it to the backend API.

This includes:

- Setting up a React + Vite + TypeScript project
- Creating page structure and routing
- Building an API layer
- Fetching data from the backend
- Implementing the customer package builder flow
- Submitting quotes and displaying results
- Collecting customer details (name, email, phone number)

This marks the transition to a **full-stack application**.

---

## 1. Frontend Setup

We created a frontend using:

- React
- Vite
- TypeScript

### Why this stack?

- Fast development environment (Vite)
- Type safety (TypeScript)
- Component-based UI (React)

---

## 2. Routing (React Router)

We added routing using React Router.

### Pages include:

- HomePage
- PackageBuilderPage
- QuoteSummaryPage

### Purpose

- Enables navigation between views
- Supports dynamic routes (e.g., /quote/{id})

---

## 3. API Layer (Axios)

We created a dedicated API layer using Axios.

### Responsibilities

- Centralize API calls
- Handle HTTP requests to backend
- Keep components clean

### Example responsibilities

- Fetch event types
- Fetch upgrades
- Submit quote
- Fetch quote by ID

---

## 4. Backend CORS Configuration

We configured CORS in Spring Boot to allow frontend requests.

### Why?

- Frontend runs on a different port (e.g., localhost:5173)
- Backend runs on localhost:8080
- Browser blocks requests without CORS configuration

---

## 5. Fetching Data

We connected frontend to backend endpoints:

- GET /api/event-types
- GET /api/upgrades

### Result

- Data is loaded dynamically from backend
- No hardcoded frontend data

---

## 6. PackageBuilderPage

This page allows users to build their event package.

### Features

- Select event type
- Enter guest count
- Select upgrades

### State Management

- Selected event type
- Guest count
- Selected upgrades

---

## 7. Live Price Preview

We implemented a live price preview on the frontend.

### How it works

- Calculates estimated price based on:
    - Event type base price
    - Selected upgrades
    - Guest count

### Important Note

- This is only a preview
- Final price is calculated in the backend

---

## 8. Submitting a Quote

When the user submits:

1. Frontend sends POST request to:

   /api/quotes

2. Backend:
    - Validates input
    - Calculates final price
    - Saves quote
    - Returns quote ID

---

## 9. Customer Details Input

We added support for collecting customer information in the frontend.

### Fields

- Customer name
- Customer email
- Customer phone number

### Behavior

- Inputs are managed using React state
- Basic validation ensures all fields are filled before submission

### Backend integration

- Data is sent as part of:

  POST /api/quotes

- Backend validates:
    - Required fields
    - Email format
    - Phone number format

---

## 10. Redirect to Quote Summary

After submission:

- Frontend redirects to:

  /quote/{id}

This allows viewing the created quote.

---

## 11. QuoteSummaryPage

This page displays the final quote details.

### Behavior

- Fetches quote using:

  GET /api/quotes/{id}

- Displays:
    - Customer details
    - Event type
    - Selected upgrades
    - Final backend-calculated price
    - Status

---

## 12. Full Customer Flow

The complete flow is now:

1. User selects event options
2. User enters:
    - Name
    - Email
    - Phone number
3. User sees live price preview
4. User submits quote
5. Backend processes and saves quote
6. User is redirected to summary page 
7. Final quote is fetched and displayed

---

## Result

At the end of this stage:

- Frontend is fully connected to backend
- Customer can build and submit a quote
- Data flows end-to-end across the system
- Application is now full-stack

---

## Next Step

- Build admin functionality (quotes management)
- Improve UI/UX
- Add better validation and feedback