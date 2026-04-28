# Backup Strategy — Event Hall Package Builder

## Purpose

This document defines how application data is protected, backed up, and restored in a production environment.

Reliable backups are critical when deploying the system for real clients.

---

## What Needs Backup

The system stores critical business data in PostgreSQL:

- Customers
- Quotes
- QuoteItems (selected upgrades)
- Event types
- Upgrades

Loss of this data would directly impact client operations.

---

## Backup Approach

### Primary Strategy

Use managed PostgreSQL with automatic backups.

Recommended platforms:

- Render PostgreSQL
- Railway PostgreSQL
- AWS RDS (future upgrade)

These provide:

- Automatic daily backups
- Point-in-time recovery (depending on plan)
- Managed reliability

---

## Backup Frequency

| Type         | Frequency |
|--------------|----------|
| Full backup  | Daily    |
| Retention    | 7–30 days |

---

## Manual Backup (Optional)

Create backup:

pg_dump -U postgres -d eventhall_db > backup.sql

Restore backup:

psql -U postgres -d eventhall_db < backup.sql

---

## Recovery Plan

1. Identify failure time
2. Restore database using platform tools
3. Restart backend
4. Verify:
   - Quotes load correctly
   - Dashboard works
   - New quotes can be created

---

## Per-Client Isolation

Each client should have a separate database:

client_a_db
client_b_db
client_c_db

Benefits:

- Data isolation
- Safer backups
- Easier recovery
- Better scalability

---

## Deployment Considerations

Each client deployment must:

- Use its own database credentials
- Have backups enabled
- Be verified after deployment

---

## Future Improvements

- Automated backup verification
- Scheduled exports (S3)
- Monitoring and alerts

---

## Summary

The system uses:

- Managed PostgreSQL backups
- Optional manual backups
- Per-client database isolation

Ensuring:

- Data safety
- Business continuity
- Production readiness
