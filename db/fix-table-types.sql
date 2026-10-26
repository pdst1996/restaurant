-- Corrección del esquema parcial creado con IDs String y tamaños ordinales.
-- Ejecutar una vez antes de iniciar la aplicación con ddl-auto=update.
-- No elimina tablas ni registros; cualquier conversión inválida revierte todo.
BEGIN;

SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';

DO $$
DECLARE
    size_constraint text;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'orders'
          AND column_name = 'waiter_id'
          AND data_type IN ('character varying', 'character', 'text')
    ) THEN
        ALTER TABLE public.orders
            ALTER COLUMN waiter_id TYPE bigint USING waiter_id::bigint;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'order_items'
          AND column_name = 'size'
          AND data_type IN ('smallint', 'integer', 'bigint')
    ) THEN
        -- La restricción ordinal anterior no es compatible con los nombres.
        FOR size_constraint IN
            SELECT c.conname
            FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid
                AND a.attnum = ANY(c.conkey)
            WHERE c.conrelid = 'public.order_items'::regclass
              AND c.contype = 'c' AND a.attname = 'size'
        LOOP
            EXECUTE format('ALTER TABLE public.order_items DROP CONSTRAINT %I', size_constraint);
        END LOOP;

        ALTER TABLE public.order_items
            ALTER COLUMN size TYPE varchar(255) USING (
                CASE size
                    WHEN 0 THEN 'HALF'
                    WHEN 1 THEN 'WHOLE'
                    WHEN 2 THEN 'BIG'
                    WHEN 3 THEN 'SMALL'
                    WHEN 4 THEN 'MEDIUM'
                END
            );

        ALTER TABLE public.order_items
            ADD CONSTRAINT order_items_size_check
            CHECK (size IN ('HALF', 'WHOLE', 'BIG', 'SMALL', 'MEDIUM'));
    END IF;
END $$;

COMMIT;
