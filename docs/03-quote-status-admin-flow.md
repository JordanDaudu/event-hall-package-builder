# 03 — Quote Status & Admin Flow

## Overview

In this stage, we extended the quote system to support **status management** and basic **admin functionality**.

This introduces:
- State management using enums
- Admin-specific endpoints
- Updating existing resources

---

## 1. QuoteStatus Enum

We introduced a QuoteStatus enum to represent the state of a quote.

Example values:

- NEW
- CONTACTED
- APPROVED
- REJECTED

### Why use an enum?

- Restricts values to a fixed set
- Prevents invalid states
- Improves code readability
- Aligns with real-world business flow

---

## 2. Updating QuoteResponse

The QuoteResponse DTO was updated to include:

- status

This allows the client (frontend or admin) to see the current state of the quote.

---

## 3. Admin Endpoint — Update Quote Status

Endpoint:

- PUT /api/admin/quotes/{id}/status

### Purpose

Allows an admin to update the status of an existing quote.

---

## 4. How the Flow Works

1. Admin sends request with:
    - quote ID (path variable)
    - new status (request body or parameter)

2. Controller receives request

3. Service layer:
    - Finds the quote by ID
    - Validates that it exists
    - Updates the status
    - Returns updated response

---

## 5. Why Use PUT?

PUT is used because:

- We are updating an existing resource
- The operation is idempotent (same request = same result)
- It follows REST conventions

---

## 6. Immutability with Records

DTOs in this project are implemented using Java records.

Example:

    public record QuoteResponse(
        Long id,
        String customerName,
        String customerEmail,
        int guestCount,
        double totalPrice,
        QuoteStatus status
    ) {}

### Why records?

- Immutable by default
- Less boilerplate (no getters/setters)
- Safer data handling
- Clear intent: data carriers only

---

## 7. Business Logic Responsibility

The service layer is responsible for:

- Finding quotes
- Updating status
- Handling invalid IDs
- Returning consistent responses

This keeps controllers thin and focused.

---

## 8. Error Handling

If a quote is not found:

- The service throws an exception
- GlobalExceptionHandler converts it into a proper HTTP response

This ensures:

- Clean API responses
- No internal errors exposed

---

## Result

At the end of this stage:

- Quotes now have a lifecycle (status)
- Admins can manage quote progress
- The API follows REST conventions for updates
- DTOs remain immutable and safe

---

## Next Step

Persist quotes in a database:

- Replace in-memory storage
- Store status in the database
- Enable long-term data management