BEGIN;
-- Export the audit history before rollback. Existing order statuses are unchanged.
DROP TABLE IF EXISTS order_status_history;
COMMIT;
