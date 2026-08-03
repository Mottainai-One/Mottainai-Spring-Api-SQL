CREATE SCHEMA IF NOT EXISTS mottainai;
CREATE SCHEMA IF NOT EXISTS mottainai_analytics;

SET search_path TO mottainai, public;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'purchase_order_status'
          AND typnamespace = 'mottainai'::regnamespace
    ) THEN
        CREATE TYPE purchase_order_status AS ENUM ('PENDING', 'APPROVED', 'CANCELED');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'receiving_status'
          AND typnamespace = 'mottainai'::regnamespace
    ) THEN
        CREATE TYPE receiving_status AS ENUM ('PENDING', 'CONFIRMED', 'DIVERGENT');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'movement_type'
          AND typnamespace = 'mottainai'::regnamespace
    ) THEN
        CREATE TYPE movement_type AS ENUM ('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER', 'DONATION', 'DISPOSAL');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'inventory_type'
          AND typnamespace = 'mottainai'::regnamespace
    ) THEN
        CREATE TYPE inventory_type AS ENUM ('NORMAL', 'CONSIGNED', 'QUARANTINE');
    END IF;
END $$;

CREATE OR REPLACE FUNCTION fn_validate_cpf(p_cpf CHAR(11))
RETURNS BOOLEAN AS $$
DECLARE
    v_cpf TEXT;
    v_sum INTEGER := 0;
    v_remainder INTEGER;
    v_digit1 INTEGER;
    v_digit2 INTEGER;
    v_i INTEGER;
BEGIN
    v_cpf := regexp_replace(p_cpf, '[^0-9]', '', 'g');

    IF LENGTH(v_cpf) != 11 OR v_cpf ~ '^(\d)\1{10}$' THEN
        RETURN FALSE;
    END IF;

    FOR v_i IN 1..9 LOOP
        v_sum := v_sum + CAST(SUBSTRING(v_cpf, v_i, 1) AS INTEGER) * (11 - v_i);
    END LOOP;

    v_remainder := (v_sum * 10) % 11;
    v_digit1 := CAST(SUBSTRING(v_cpf, 10, 1) AS INTEGER);
    IF v_digit1 != (CASE WHEN v_remainder = 10 THEN 0 ELSE v_remainder END) THEN
        RETURN FALSE;
    END IF;

    v_sum := 0;
    FOR v_i IN 1..10 LOOP
        v_sum := v_sum + CAST(SUBSTRING(v_cpf, v_i, 1) AS INTEGER) * (12 - v_i);
    END LOOP;

    v_remainder := (v_sum * 10) % 11;
    v_digit2 := CAST(SUBSTRING(v_cpf, 11, 1) AS INTEGER);
    RETURN v_digit2 = CASE WHEN v_remainder = 10 THEN 0 ELSE v_remainder END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION fn_validate_email(p_email VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN p_email IS NULL
        OR p_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION fn_validate_cnpj(p_cnpj CHAR(14))
RETURNS BOOLEAN AS $$
DECLARE
    v_cnpj TEXT;
    v_sum INTEGER := 0;
    v_remainder INTEGER;
    v_weight INTEGER;
    v_i INTEGER;
BEGIN
    v_cnpj := regexp_replace(p_cnpj, '[^0-9]', '', 'g');

    IF LENGTH(v_cnpj) != 14 OR v_cnpj ~ '^(\d)\1{13}$' THEN
        RETURN FALSE;
    END IF;

    v_weight := 5;
    FOR v_i IN 1..12 LOOP
        v_sum := v_sum + CAST(SUBSTRING(v_cnpj, v_i, 1) AS INTEGER) * v_weight;
        v_weight := CASE WHEN v_weight = 2 THEN 9 ELSE v_weight - 1 END;
    END LOOP;
    v_remainder := v_sum % 11;
    IF CAST(SUBSTRING(v_cnpj, 13, 1) AS INTEGER) != (CASE WHEN v_remainder < 2 THEN 0 ELSE 11 - v_remainder END) THEN
        RETURN FALSE;
    END IF;

    v_sum := 0;
    v_weight := 6;
    FOR v_i IN 1..13 LOOP
        v_sum := v_sum + CAST(SUBSTRING(v_cnpj, v_i, 1) AS INTEGER) * v_weight;
        v_weight := CASE WHEN v_weight = 2 THEN 9 ELSE v_weight - 1 END;
    END LOOP;
    v_remainder := v_sum % 11;
    RETURN CAST(SUBSTRING(v_cnpj, 14, 1) AS INTEGER) = CASE WHEN v_remainder < 2 THEN 0 ELSE 11 - v_remainder END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
