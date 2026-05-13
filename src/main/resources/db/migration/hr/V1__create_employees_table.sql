CREATE SCHEMA IF NOT EXISTS hr;

CREATE TABLE hr.employees
(
    uuid        UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  VARCHAR(100)       NOT NULL,
    last_name   VARCHAR(100)       NOT NULL,
    middle_name VARCHAR(100),
    role        VARCHAR(50)        NOT NULL,
    active      BOOLEAN            NOT NULL DEFAULT TRUE,
    email       VARCHAR(255)       NOT NULL UNIQUE,
    phone       VARCHAR(50)        NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ        NOT NULL DEFAULT now()
);