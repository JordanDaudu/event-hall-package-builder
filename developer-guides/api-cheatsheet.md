# API Cheat Sheet

## Public Endpoints

GET /api/event-types  
GET /api/upgrades  
POST /api/quotes  
GET /api/quotes/{id}

---

## Admin Endpoints

GET /api/admin/quotes  
GET /api/admin/quotes?status=NEW  
PUT /api/admin/quotes/{id}/status

GET /api/admin/upgrades  
POST /api/admin/upgrades  
PUT /api/admin/upgrades/{id}  
DELETE /api/admin/upgrades/{id}

---

## Quote Request Example

{
"eventTypeId": 1,
"guestCount": 100,
"upgradeIds": [1, 2],
"customerName": "Jordan",
"customerEmail": "jordan@example.com",
"customerPhoneNumber": "0501234567"
}