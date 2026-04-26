# 02 — In-Memory API (MVP)

## Overview

In this stage, we implemented a working backend API using **in-memory data** instead of a database.

This allowed us to:
- Build and test the full request/response flow
- Implement business logic
- Validate inputs
- Test APIs using Swagger

This is a common development approach before introducing a real database.

---

## Implemented Features

### 1. Project Structure

We created a layered architecture:

- controller → handles HTTP requests
- service → contains business logic
- dto → defines request/response models

---

### 2. DTOs (Data Transfer Objects)

We introduced DTOs to control the data sent between client and server.

Examples:

- CreateQuoteRequest
- QuoteResponse
- UpgradeDto
- EventTypeDto

### Why use DTOs?

- Prevent exposing internal models
- Control API structure
- Improve security and flexibility

---

### 3. In-Memory Data (Fake Data)

Instead of using a database, we stored data inside services using lists.

Example concept:

- List of event types
- List of upgrades
- List of quotes

### Why?

- Faster development
- No database setup required
- Easier debugging

---

### 4. Services

We implemented service classes to handle business logic.

Responsibilities:

- Provide event types
- Provide upgrades
- Handle quote creation
- Calculate pricing
- Store quotes in memory

---

### 5. Public GET Endpoints

We created endpoints for retrieving available options:

- GET /api/event-types
- GET /api/upgrades

These return lists from in-memory data.

---

### 6. Quote Creation Endpoint

Endpoint:

- POST /api/quotes

This endpoint:

1. Receives a CreateQuoteRequest
2. Validates the input
3. Calculates total price
4. Stores the quote in memory
5. Returns a QuoteResponse

---

### 7. Backend Price Calculation

Price calculation is handled in the service layer.

### Why in backend?

- Prevents manipulation from frontend
- Ensures consistent pricing logic
- Centralizes business rules

---

### 8. Validation

We added validation using annotations and @Valid.

Examples:

- @NotNull
- @NotBlank
- @Email

Validation is triggered automatically when a request is received.

---

### 9. Global Exception Handling

We implemented a GlobalExceptionHandler.

Responsibilities:

- Catch validation errors
- Return structured error responses
- Prevent raw exceptions from reaching the client

---

### 10. Swagger UI

Swagger UI was added for API testing and documentation.

Benefits:

- Test endpoints without external tools
- View request/response models
- Explore API structure visually

---

### 11. Get Quote by ID

Endpoint:

- GET /api/quotes/{id}

Returns a specific quote from in-memory storage.

---

### 12. Admin: Get All Quotes

Endpoint:

- GET /api/admin/quotes

Returns all stored quotes.

Used for admin viewing.

---

## Current Limitations

- Data is not persistent (resets on restart)
- No database integration yet
- No authentication for admin endpoints

---

## Result

At the end of this stage:

- The backend API is fully functional
- Full request flow is implemented
- Business logic is centralized in services
- Validation and error handling are in place
- APIs can be tested via Swagger

---

## Next Step

Replace in-memory data with a real database:

- PostgreSQL setup
- JPA entities
- Repositories
- Persisting quotes