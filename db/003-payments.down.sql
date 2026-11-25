BEGIN;
-- Export payment metadata and reconcile pending/refunded entries before rollback.
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_order_idempotency_uk;
DROP INDEX IF EXISTS payments_order_idempotency_uk;
ALTER TABLE payments DROP COLUMN IF EXISTS request_fingerprint, DROP COLUMN IF EXISTS idempotency_key,
    DROP COLUMN IF EXISTS refund_reason, DROP COLUMN IF EXISTS reversed_from,
    DROP COLUMN IF EXISTS refunded_at, DROP COLUMN IF EXISTS refunded_by,
    DROP COLUMN IF EXISTS collected_by, DROP COLUMN IF EXISTS refund_reference, DROP COLUMN IF EXISTS external_reference,
    DROP COLUMN IF EXISTS cash_change, DROP COLUMN IF EXISTS cash_received, DROP COLUMN IF EXISTS tip,
    DROP COLUMN IF EXISTS status;
ALTER TABLE orders DROP COLUMN IF EXISTS tax_rate;
COMMIT;
