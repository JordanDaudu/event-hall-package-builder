# Event Hall Package Builder — API Cheat Sheet

## Base URL

Local backend:

    http://localhost:8080

All API routes start with:

    /api

---

# 1. Public Event Type Endpoints

## Get all event types

### Request

    GET /api/event-types

### Purpose

Returns all available event types.

### Example Response

```json
[
  {
    "id": 1,
    "name": "Wedding",
    "basePrice": 120.00
  },
  {
    "id": 2,
    "name": "Birthday",
    "basePrice": 80.00
  }
]
```

### Frontend Usage

Used in `PackageBuilderPage` to let the customer choose the event type.

---

# 2. Public Upgrade Endpoints

## Get active upgrades

### Request

    GET /api/upgrades

### Purpose

Returns only active upgrades.

### Example Response

```json
[
  {
    "id": 1,
    "name": "Flowers",
    "description": "Premium flower arrangements",
    "category": "Decor",
    "price": 2500.00,
    "active": true
  },
  {
    "id": 2,
    "name": "DJ",
    "description": "Professional DJ service",
    "category": "Entertainment",
    "price": 3500.00,
    "active": true
  }
]
```

### Frontend Usage

Used in `PackageBuilderPage` to display selectable add-ons.

---

# 3. Quote Endpoints

## Create quote

### Request

    POST /api/quotes

### Purpose

Creates a customer quote request.

The backend calculates the final price.

### Request Body

```json
{
  "eventTypeId": 1,
  "guestCount": 100,
  "upgradeIds": [1, 2],
  "customerName": "Jordan",
  "customerEmail": "jordan@example.com"
}
```

### Example Response

```json
{
  "id": 1,
  "eventTypeName": "Wedding",
  "guestCount": 100,
  "upgrades": ["Flowers", "DJ"],
  "totalPrice": 18000.00,
  "status": "NEW"
}
```

### Price Formula

    totalPrice = (eventType.basePrice * guestCount) + selectedUpgradesTotal

Example:

    Wedding: 120 * 100 = 12000
    Flowers + DJ = 2500 + 3500 = 6000
    Total = 18000

### Frontend Usage

Used when customer submits the package builder form.

---

## Get quote by ID

### Request

    GET /api/quotes/{id}

### Example

    GET /api/quotes/1

### Purpose

Returns one quote by ID.

### Example Response

```json
{
  "id": 1,
  "eventTypeName": "Wedding",
  "guestCount": 100,
  "upgrades": ["Flowers", "DJ"],
  "totalPrice": 18000.00,
  "status": "NEW"
}
```

### Frontend Usage

Used by `QuoteSummaryPage`.

---

# 4. Admin Quote Endpoints

## Get all quotes

### Request

    GET /api/admin/quotes

### Purpose

Returns all submitted quotes.

### Example Response

```json
[
  {
    "id": 1,
    "eventTypeName": "Wedding",
    "guestCount": 100,
    "upgrades": ["Flowers", "DJ"],
    "totalPrice": 18000.00,
    "status": "NEW"
  }
]
```

### Frontend Usage

Used in `AdminQuotesPage`.

---

## Filter quotes by status

### Request

    GET /api/admin/quotes?status=NEW

### Supported Status Values

    NEW
    CONTACTED
    APPROVED
    REJECTED

### Example

    GET /api/admin/quotes?status=CONTACTED

### Purpose

Returns only quotes matching the selected status.

### Example Response

```json
[
  {
    "id": 1,
    "eventTypeName": "Wedding",
    "guestCount": 100,
    "upgrades": ["Flowers", "DJ"],
    "totalPrice": 18000.00,
    "status": "CONTACTED"
  }
]
```

### Frontend Usage

Used for admin filtering in `AdminQuotesPage`.

---

## Update quote status

### Request

    PUT /api/admin/quotes/{id}/status

### Example

    PUT /api/admin/quotes/1/status

### Request Body

```json
{
  "status": "CONTACTED"
}
```

### Example Response

```json
{
  "id": 1,
  "eventTypeName": "Wedding",
  "guestCount": 100,
  "upgrades": ["Flowers", "DJ"],
  "totalPrice": 18000.00,
  "status": "CONTACTED"
}
```

### Frontend Usage

Used when admin changes a quote status.

---

# 5. Admin Upgrade Endpoints

## Create upgrade

### Request

    POST /api/admin/upgrades

### Request Body

```json
{
  "name": "Premium Food Package",
  "description": "Expanded menu with premium dishes",
  "category": "Food",
  "price": 6000
}
```

### Example Response

```json
{
  "id": 6,
  "name": "Premium Food Package",
  "description": "Expanded menu with premium dishes",
  "category": "Food",
  "price": 6000.00,
  "active": true
}
```

### Frontend Usage

Used in `AdminUpgradesPage`.

---

## Update upgrade

### Request

    PUT /api/admin/upgrades/{id}

### Example

    PUT /api/admin/upgrades/6

### Request Body

```json
{
  "name": "Premium Food Package",
  "description": "Expanded menu with premium dishes and dessert",
  "category": "Food",
  "price": 7000,
  "active": true
}
```

### Example Response

```json
{
  "id": 6,
  "name": "Premium Food Package",
  "description": "Expanded menu with premium dishes and dessert",
  "category": "Food",
  "price": 7000.00,
  "active": true
}
```

### Frontend Usage

Used when admin edits upgrade details.

---

## Soft delete upgrade

### Request

    DELETE /api/admin/upgrades/{id}

### Example

    DELETE /api/admin/upgrades/6

### Purpose

Soft deletes an upgrade by setting:

    active = false

The row remains in the database, but public users no longer see it.

### Response

No response body.

### Frontend Usage

Used when admin removes an upgrade from customer-facing options.

---

# 6. Validation Rules

## CreateQuoteRequest

| Field | Rule |
|---|---|
| eventTypeId | required |
| guestCount | minimum 1 |
| upgradeIds | required list |
| customerName | required |
| customerEmail | required, valid email |

---

## CreateUpgradeRequest

| Field | Rule |
|---|---|
| name | required |
| description | required |
| category | required |
| price | required, positive |

---

## UpdateUpgradeRequest

| Field | Rule |
|---|---|
| name | required |
| description | required |
| category | required |
| price | required, positive |
| active | required |

---

## UpdateQuoteStatusRequest

| Field | Rule |
|---|---|
| status | required, must be valid QuoteStatus |

---

# 7. Error Examples

## Validation error

### Request

```json
{
  "eventTypeId": null,
  "guestCount": 0,
  "upgradeIds": null,
  "customerName": "",
  "customerEmail": "not-an-email"
}
```

### Response

```json
{
  "eventTypeId": "Event type is required",
  "guestCount": "Guest count must be at least 1",
  "upgradeIds": "Upgrade IDs list is required",
  "customerName": "Customer name is required",
  "customerEmail": "Customer email must be valid"
}
```

---

## Invalid enum/status error

### Request

```json
{
  "status": "DONE"
}
```

### Response

```json
{
  "error": "Invalid request body. Check that all fields use the correct format and enum values."
}
```

---

# 8. Quote Status Values

Valid quote statuses:

    NEW
    CONTACTED
    APPROVED
    REJECTED

### Meaning

| Status | Meaning |
|---|---|
| NEW | Quote was submitted but not handled yet |
| CONTACTED | Admin contacted the customer |
| APPROVED | Quote was approved |
| REJECTED | Quote was rejected |

---

# 9. Frontend Flow Reference

## Customer Flow

1. Fetch event types:

       GET /api/event-types

2. Fetch upgrades:

       GET /api/upgrades

3. Customer selects event type, guest count, and upgrades.

4. Frontend may show live price preview.

5. Submit quote:

       POST /api/quotes

6. Show backend-confirmed quote result.

---

## Admin Quote Flow

1. Fetch all quotes:

       GET /api/admin/quotes

2. Filter by status:

       GET /api/admin/quotes?status=NEW

3. Update quote status:

       PUT /api/admin/quotes/{id}/status

---

## Admin Upgrade Flow

1. Fetch active upgrades:

       GET /api/upgrades

2. Create upgrade:

       POST /api/admin/upgrades

3. Update upgrade:

       PUT /api/admin/upgrades/{id}

4. Soft delete upgrade:

       DELETE /api/admin/upgrades/{id}

---

# 10. Important Backend Rule

The frontend can calculate a price preview for user experience.

However, the final price must always be calculated by the backend.

Reason:

    Frontend values can be manipulated by users.

The backend is the source of truth.