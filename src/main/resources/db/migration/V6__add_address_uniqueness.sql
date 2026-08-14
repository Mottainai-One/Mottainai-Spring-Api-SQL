SET search_path TO mottainai, public;

CREATE UNIQUE INDEX ux_address_active_location
    ON address (zip_code, street, number, COALESCE(complement, ''))
    WHERE deleted_at IS NULL;
