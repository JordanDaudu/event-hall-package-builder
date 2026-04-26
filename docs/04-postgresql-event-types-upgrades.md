# 04 — PostgreSQL Integration (EventTypes & Upgrades)

## Overview

In this stage, we began migrating from in-memory data to a real database using PostgreSQL and Spring Data JPA.

This is the first step toward persistent storage and introduces:

- Database configuration
- JPA entities
- Repositories
- Data initialization
- Replacing fake services with database-backed services

---

## 1. JPA & PostgreSQL Setup

We configured the application to connect to a PostgreSQL database.

### Key additions:

- PostgreSQL driver dependency
- Spring Data JPA dependency
- Database configuration in application.properties

Example configuration:

    spring.datasource.url=jdbc:postgresql://localhost:5432/eventhall
    spring.datasource.username=your_username
    spring.datasource.password=your_password

    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true

### What this enables:

- Automatic table creation (via Hibernate)
- Object → Table mapping
- Query abstraction using repositories

---

## 2. EventType Entity

We replaced the in-memory EventType model with a JPA entity.

Example structure:

- id (primary key)
- name
- description
- basePrice

### Key annotations:

- @Entity → marks class as a database table
- @Id → primary key
- @GeneratedValue → auto-generates ID

---

## 3. Upgrade Entity

We created a JPA entity for upgrades.

Fields include:

- id
- name
- description
- category
- price
- active

### Purpose:

Represents optional features customers can add to their event package.

---

## 4. Repositories

We introduced Spring Data JPA repositories:

- EventTypeRepository
- UpgradeRepository

These extend:

    JpaRepository<Entity, Long>

### Benefits:

- No need to write SQL manually
- Built-in CRUD operations
- Easy querying

---

## 5. DataInitializer

We added a DataInitializer component to populate the database with initial data.

### Responsibilities:

- Insert default event types
- Insert default upgrades
- Run on application startup

### Why?

- Ensures the app has usable data immediately
- Useful for development and testing

---

## 6. DTO Mapping

We continued using DTOs and mapped entities to DTOs manually.

Example flow:

    Entity → Service → DTO → Controller → Client

### Why not return entities directly?

- Avoid exposing internal structure
- Maintain flexibility
- Prevent tight coupling between API and database

---

## 7. Replacing In-Memory Services

We updated services to use repositories instead of in-memory lists.

Before:

- Data stored in List<> inside service

After:

- Data fetched using repository methods

Example:

    upgradeRepository.findAll()

### Result:

- Data is now persistent
- Changes survive application restart

---

## Current Scope

At this stage:

- EventTypes are stored in PostgreSQL
- Upgrades are stored in PostgreSQL
- Quotes are still in-memory (temporary)

---

## Result

- Database integration is successfully introduced
- Entities and repositories are working
- Application now uses persistent data for core resources

---

## Next Step

Persist quotes in the database and introduce relationships:

- Customer → Quote
- EventType → Quote
- Quote → QuoteItem
- QuoteItem → Upgrade

This will complete the full backend data model.