-- Revisar el catálogo y respaldar antes de migrar.
-- Convierte enums ordinales y enlaza elementos antiguos por su código de tipo.
-- No elimina registros ni columnas. Cualquier referencia desconocida revierte todo.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';

DO $$
DECLARE
    constraint_name text;
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'restaurant_tables'
          AND column_name = 'shape' AND data_type IN ('smallint', 'integer', 'bigint')) THEN
        IF EXISTS (SELECT 1 FROM public.restaurant_tables WHERE shape NOT IN (0, 1)) THEN
            RAISE EXCEPTION 'Hay formas de mesa desconocidas; revisar antes de migrar';
        END IF;
        FOR constraint_name IN
            SELECT c.conname FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
            WHERE c.conrelid = 'public.restaurant_tables'::regclass
              AND c.contype = 'c' AND a.attname = 'shape'
        LOOP
            EXECUTE format('ALTER TABLE public.restaurant_tables DROP CONSTRAINT %I', constraint_name);
        END LOOP;
        ALTER TABLE public.restaurant_tables ALTER COLUMN shape DROP DEFAULT;
        ALTER TABLE public.restaurant_tables ALTER COLUMN shape TYPE varchar(255)
            USING CASE shape WHEN 0 THEN 'RECTANGULAR' WHEN 1 THEN 'CIRCULAR' END;
        ALTER TABLE public.restaurant_tables ADD CONSTRAINT restaurant_tables_shape_check
            CHECK (shape IN ('RECTANGULAR', 'CIRCULAR'));
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'restaurant_tables'
          AND column_name = 'table_status' AND data_type IN ('smallint', 'integer', 'bigint')) THEN
        IF EXISTS (SELECT 1 FROM public.restaurant_tables WHERE table_status NOT IN (0, 1, 2)) THEN
            RAISE EXCEPTION 'Hay estados de mesa desconocidos; revisar antes de migrar';
        END IF;
        FOR constraint_name IN
            SELECT c.conname FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
            WHERE c.conrelid = 'public.restaurant_tables'::regclass
              AND c.contype = 'c' AND a.attname = 'table_status'
        LOOP
            EXECUTE format('ALTER TABLE public.restaurant_tables DROP CONSTRAINT %I', constraint_name);
        END LOOP;
        ALTER TABLE public.restaurant_tables ALTER COLUMN table_status DROP DEFAULT;
        ALTER TABLE public.restaurant_tables ALTER COLUMN table_status TYPE varchar(255)
            USING CASE table_status WHEN 0 THEN 'FREE' WHEN 1 THEN 'OCCUPIED' WHEN 2 THEN 'OUT_OF_SERVICE' END;
        ALTER TABLE public.restaurant_tables ADD CONSTRAINT restaurant_tables_status_check
            CHECK (table_status IN ('FREE', 'OCCUPIED', 'OUT_OF_SERVICE'));
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'element' AND column_name = 'type') THEN
        ALTER TABLE public.element ADD COLUMN IF NOT EXISTS element_type_id bigint;
        UPDATE public.element e SET element_type_id = t.id
            FROM public.element_type t WHERE e.type = t.type AND e.element_type_id IS NULL;
        IF EXISTS (SELECT 1 FROM public.element WHERE element_type_id IS NULL) THEN
            RAISE EXCEPTION 'Hay elementos sin tipo en element_type; completar el catálogo antes de migrar';
        END IF;
        ALTER TABLE public.element ALTER COLUMN element_type_id SET NOT NULL;
        -- Se conserva type como dato histórico, sin exigirlo a las nuevas inserciones.
        ALTER TABLE public.element ALTER COLUMN type DROP NOT NULL;
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
            WHERE c.conrelid = 'public.element'::regclass AND c.contype = 'f' AND a.attname = 'element_type_id'
        ) THEN
            ALTER TABLE public.element ADD CONSTRAINT element_element_type_fk
                FOREIGN KEY (element_type_id) REFERENCES public.element_type(id);
        END IF;
    END IF;
END $$;

COMMIT;
