SET search_path TO mottainai, public;

CREATE TABLE IF NOT EXISTS subscription_plan (
    plan_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    store_limit INTEGER NOT NULL CHECK (store_limit > 0),
    user_limit INTEGER NOT NULL CHECK (user_limit > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS address (
    address_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zip_code CHAR(8) NOT NULL CHECK (zip_code ~ '^\d{8}$'),
    street VARCHAR(150) NOT NULL,
    number VARCHAR(10) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL CHECK (state ~ '^[A-Z]{2}$'),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS company (
    company_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id INTEGER NOT NULL REFERENCES subscription_plan(plan_id) ON DELETE RESTRICT,
    official_name VARCHAR(150) NOT NULL,
    trade_name VARCHAR(150),
    cnpj CHAR(14) NOT NULL UNIQUE CHECK (fn_validate_cnpj(cnpj)),
    email VARCHAR(150) NOT NULL CHECK (fn_validate_email(email)),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee_role (
    role_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description TEXT,
    permission_level INTEGER NOT NULL CHECK (permission_level >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS retail_store (
    store_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES company(company_id) ON DELETE RESTRICT,
    address_id INTEGER NOT NULL REFERENCES address(address_id) ON DELETE RESTRICT,
    name VARCHAR(120) NOT NULL,
    cnpj CHAR(14) NOT NULL UNIQUE CHECK (fn_validate_cnpj(cnpj)),
    email VARCHAR(150) CHECK (fn_validate_email(email)),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee (
    employee_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id INTEGER NOT NULL REFERENCES retail_store(store_id) ON DELETE RESTRICT,
    role_id INTEGER NOT NULL REFERENCES employee_role(role_id) ON DELETE RESTRICT,
    name VARCHAR(150) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE CHECK (fn_validate_cpf(cpf)),
    email VARCHAR(150) CHECK (fn_validate_email(email)),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    hire_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_user (
    user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id INTEGER NOT NULL UNIQUE REFERENCES employee(employee_id) ON DELETE RESTRICT,
    email VARCHAR(150) NOT NULL UNIQUE CHECK (fn_validate_email(email)),
    password_hash VARCHAR(255) NOT NULL,
    last_login TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
