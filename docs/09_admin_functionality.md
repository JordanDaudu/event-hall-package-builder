# 09 — Admin Functionality

## Overview

In this stage, we introduced the admin-side functionality of the application.

This includes:

- Viewing all submitted quotes
- Updating quote statuses
- Filtering quotes by status
- Managing upgrades (active/inactive)
- Adding a dedicated admin API endpoint

This completes the **core business workflow** of the system.

---

## 1. Admin Quotes Page

We implemented a dedicated admin interface for managing quotes.

### Features

- View all submitted quotes
- Display customer details:
    - Name
    - Email
    - Phone number
- Display event details:
    - Event type
    - Guest count
    - Selected upgrades
    - Total price
- Display current quote status

---

## 2. Updating Quote Status

Admins can update the status of a quote directly from the UI.

### Available statuses

- NEW
- CONTACTED
- APPROVED
- REJECTED

### Behavior

- Selecting a new status triggers:

  PUT /api/admin/quotes/{id}/status

- The UI updates immediately after the backend responds

---

## 3. Filtering Quotes by Status

We added filtering functionality to improve admin workflow.

### UI

- Dropdown filter with options:
    - ALL
    - NEW
    - CONTACTED
    - APPROVED
    - REJECTED

### Backend integration

- Uses:

  GET /api/admin/quotes?status={status}

### Result

- Admin can quickly focus on relevant quotes
- Reduces clutter in large datasets

---

## 4. Admin Upgrade Management (Backend)

We enhanced upgrade management for admins.

### Problem

- Public endpoint only returns active upgrades:

  GET /api/upgrades

- Admin needs to see all upgrades, including inactive ones

---

### Solution

Added new endpoint:

```
GET /api/admin/upgrades
```

### Behavior

- Returns all upgrades:
    - active = true
    - active = false

---

## 5. Active vs Inactive Upgrades

### Concept

- Upgrades are not deleted permanently
- Instead, they use:

  active = true / false

---

### Behavior

- active = true → visible to customers
- active = false → hidden from customers but visible to admin

---

### Benefits

- Prevents breaking historical quotes
- Allows re-enabling upgrades later
- Supports temporary unavailability

---

## 6. Testing Admin Upgrade Endpoint

We added a test to verify behavior.

### Test flow

1. Disable an upgrade (soft delete)
2. Verify:
    - It does NOT appear in public endpoint
    - It DOES appear in admin endpoint
3. Confirm active = false is returned

---

## 7. Full Admin Flow

The admin can now:

1. View all quotes
2. Filter quotes by status
3. Update quote status
4. View all upgrades
5. Disable/enable upgrades

---

## Result

At the end of this stage:

- Admin functionality is fully integrated
- System supports real-world workflows
- Data can be managed dynamically
- Application is now closer to production-level behavior

---

## Next Step

- Build Admin Upgrades Page (frontend UI)
- Improve UI/UX (layout, styling)
- Add loading and error states
- Prepare for deployment