SET search_path TO mottainai, public;

CREATE TABLE IF NOT EXISTS product_category (
    category_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS supplier (
    supplier_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    address_id INTEGER NOT NULL REFERENCES address(address_id) ON DELETE RESTRICT,
    trade_name VARCHAR(150) NOT NULL,
    cnpj CHAR(14) NOT NULL UNIQUE CHECK (fn_validate_cnpj(cnpj)),
    email VARCHAR(150) CHECK (fn_validate_email(email)),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    product_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id INTEGER NOT NULL REFERENCES product_category(category_id) ON DELETE RESTRICT,
    barcode VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    unit_measure VARCHAR(20) NOT NULL,
    weight DECIMAL(10, 3) CHECK (weight >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS supplier_product (
    supplier_product_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    supplier_id INTEGER NOT NULL REFERENCES supplier(supplier_id) ON DELETE RESTRICT,
    product_id INTEGER NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
    supplier_code VARCHAR(50),
    purchase_price DECIMAL(10, 2) NOT NULL CHECK (purchase_price >= 0),
    lead_time INTEGER NOT NULL CHECK (lead_time >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE (supplier_id, product_id)
);
