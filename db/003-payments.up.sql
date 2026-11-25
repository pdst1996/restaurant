BEGIN;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS tax_rate numeric(5,4);
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS status varchar(255) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS tip numeric(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS cash_received numeric(12,2),
    ADD COLUMN IF NOT EXISTS cash_change numeric(12,2),
    ADD COLUMN IF NOT EXISTS external_reference varchar(100),
    ADD COLUMN IF NOT EXISTS refund_reference varchar(100),
    ADD COLUMN IF NOT EXISTS collected_by bigint REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS refunded_by bigint REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS refunded_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS reversed_from varchar(255),
    ADD COLUMN IF NOT EXISTS refund_reason varchar(500),
    ADD COLUMN IF NOT EXISTS idempotency_key varchar(80),
    ADD COLUMN IF NOT EXISTS request_fingerprint varchar(64);
CREATE UNIQUE INDEX IF NOT EXISTS payments_order_idempotency_uk ON payments(order_id, idempotency_key);
COMMIT;
