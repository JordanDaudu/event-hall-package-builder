# 06 — Admin Upgrade Management

## Overview

In this stage, we implemented full **admin CRUD functionality** for upgrades.

This includes:

- Creating upgrades
- Updating upgrades
- Deactivating (soft deleting) upgrades
- Controlling visibility of upgrades in public APIs

This introduces an important concept: **soft delete and business-level filtering**.

---

## 1. CreateUpgradeRequest

We introduced a DTO for creating upgrades.

Fields include:

- name
- description
- category
- price

### Purpose

- Defines input structure for creating upgrades
- Keeps API clean and controlled
- Prevents direct exposure of entity

---

## 2. UpdateUpgradeRequest

We introduced a DTO for updating upgrades.

Fields include:

- name
- description
- category
- price

### Purpose

- Allows updating upgrade details safely
- Separates update logic from entity structure
- Prevents unintended modifications

---

## 3. Upgrade Entity Methods

We added behavior directly inside the Upgrade entity:

### updateDetails(...)

Updates upgrade fields such as:

- name
- description
- category
- price

### deactivate()

Sets:

- active = false

### Why put logic in the entity?

- Keeps business logic close to the data
- Prevents duplication in service layer
- Improves maintainability

---

## 4. AdminUpgradeController

We created admin endpoints:

- POST   /api/admin/upgrades
- PUT    /api/admin/upgrades/{id}
- DELETE /api/admin/upgrades/{id}

### Responsibilities

- Receive admin requests
- Validate input
- Delegate logic to service layer

---

## 5. Soft Delete Strategy

Instead of deleting upgrades from the database, we use **soft delete**.

### How it works

- DELETE endpoint does NOT remove the record
- Instead, it calls:

  upgrade.deactivate()

- This sets:

  active = false

---

### Why soft delete?

- Preserves historical data
- Prevents breaking existing quotes referencing upgrades
- Allows future reactivation if needed
- Safer than permanent deletion

---

## 6. Public API Filtering

Public endpoint:

- GET /api/upgrades

### Behavior

- Returns ONLY upgrades where:

  active = true

### Why?

- Prevents showing inactive or deprecated options to users
- Keeps API clean and user-focused
- Enforces business rules at the service level

---

## 7. Service Layer Responsibility

The service layer ensures:

- Only active upgrades are returned publicly
- Admin operations correctly modify upgrade state
- Invalid upgrade IDs are handled properly

---

## Result

At the end of this stage:

- Admin can fully manage upgrades
- Upgrades are safely deactivated instead of deleted
- Public API shows only valid options
- Business rules are enforced consistently

---