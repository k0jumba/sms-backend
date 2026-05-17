CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.users
(
    uuid            UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
    login           VARCHAR(100)       NOT NULL UNIQUE,
    password_hash   VARCHAR(100)       NOT NULL,
    role            VARCHAR(50)        NOT NULL,
    active          BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ        NOT NULL DEFAULT now()
);