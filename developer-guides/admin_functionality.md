# Admin Functionality Guide

## Quotes

### Get all quotes
GET /api/admin/quotes

### Filter by status
GET /api/admin/quotes?status=NEW

### Update status
PUT /api/admin/quotes/{id}/status

Body:
{
"status": "CONTACTED"
}

---

## Upgrades

### Get all upgrades (admin)
GET /api/admin/upgrades

Returns active + inactive upgrades

### Create upgrade
POST /api/admin/upgrades

### Update upgrade
PUT /api/admin/upgrades/{id}

### Delete (soft delete)
DELETE /api/admin/upgrades/{id}

Sets:
active = false

---

## Behavior

- Inactive upgrades are hidden from customers
- Admin can re-enable them
- No permanent deletion in V1