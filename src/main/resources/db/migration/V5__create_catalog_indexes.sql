CREATE INDEX IF NOT EXISTS idx_product_category_id
    ON product(category_id);

CREATE INDEX IF NOT EXISTS idx_supplier_product_supplier_id
    ON supplier_product(supplier_id);

CREATE INDEX IF NOT EXISTS idx_supplier_product_product_id
    ON supplier_product(product_id);

CREATE INDEX IF NOT EXISTS idx_product_active_name
    ON product(name)
    WHERE active = true AND deleted_at IS NULL;
