SET search_path TO mottainai, public;

CREATE UNIQUE INDEX ux_product_category_normalized_name
    ON product_category (LOWER(BTRIM(name)));
