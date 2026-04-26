# 05 — Database-Backed Quotes (Full Persistence)

## Overview

In this stage, we completed the migration from in-memory storage to a fully database-backed system using PostgreSQL.

This includes:

- Persisting customers
- Persisting quotes
- Persisting quote items
- Defining relationships between entities
- Replacing in-memory quote handling with JPA repositories

This is the point where the backend becomes a real-world application.

---

## 1. Customer Entity

We introduced a Customer entity to represent users submitting quotes.

Fields include:

- id
- name
- email

### Purpose

- Stores customer information
- Allows reuse of customer data across multiple quotes

---

## 2. Quote Entity

The Quote entity represents a full quote request.

Fields include:

- id
- customer (Many-to-One)
- eventType (Many-to-One)
- guestCount
- totalPrice
- status
- createdAt

### Key Relationship

- A quote belongs to one customer
- A quote belongs to one event type

---

## 3. QuoteItem Entity

QuoteItem represents a selected upgrade within a quote.

Fields include:

- id
- quote (Many-to-One)
- upgrade (Many-to-One)
- price

### Purpose

- Stores individual upgrade selections
- Allows multiple upgrades per quote

---

## 4. Entity Relationships

The system now includes the following relationships:

- Customer → Quote (One-to-Many)
- EventType → Quote (One-to-Many)
- Quote → QuoteItem (One-to-Many)
- QuoteItem → Upgrade (Many-to-One)

### Why this design?

- Normalized database structure
- Flexible for future expansion
- Avoids data duplication

---

## 5. Repositories

We added repositories for all new entities:

- CustomerRepository
- QuoteRepository
- QuoteItemRepository

Each extends:

    JpaRepository<Entity, Long>

### Benefits

- Built-in CRUD operations
- No manual SQL required
- Easy integration with Spring Boot

---

## 6. Saving Quotes (Full Flow)

When a user submits a quote:

1. Validate request
2. Find EventType from database
3. Find selected Upgrades from database
4. Create Customer (or reuse existing — optional improvement)
5. Create Quote entity
6. Create QuoteItem entities for each upgrade
7. Calculate total price
8. Save Quote (cascades to QuoteItems)
9. Return response DTO

---

## 7. Replacing In-Memory QuoteService

Before:

- Quotes stored in List<>
- Lost on application restart

After:

- Quotes stored in PostgreSQL
- Persisted across restarts
- Retrieved using QuoteRepository

---

## 8. Persistence Testing

We verified persistence by:

1. Creating a quote via API
2. Restarting the application
3. Fetching the quote again

### Result

- Data remains intact after restart
- Confirms database integration is working

---

## 9. Cascade Behavior (Important Concept)

Quote → QuoteItem relationship uses cascading.

This means:

- Saving a Quote automatically saves its QuoteItems

### Why?

- Simplifies logic
- Ensures consistency
- Reduces manual repository calls

---

## 10. DTO Mapping (Continued)

We continue mapping:

    Entity → DTO → Response

We do NOT expose entities directly.

### Why?

- Keeps API stable even if DB changes
- Prevents exposing internal structure
- Cleaner separation of concerns

---

## Result

At the end of this stage:

- Quotes are fully persisted in PostgreSQL
- Relationships between entities are implemented
- Backend supports real-world data modeling
- API behavior remains consistent with previous version

---

## Next Step

- Add advanced querying (filter quotes, search)
- Improve admin capabilities
- Introduce pagination
- Prepare for frontend integration