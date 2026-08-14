SET search_path TO mottainai, public;

UPDATE product
SET version = 1
WHERE version IS NULL;

ALTER TABLE product
    ALTER COLUMN version SET NOT NULL;
