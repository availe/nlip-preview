CREATE TYPE user_role_enum AS ENUM (
    'free_user',
    'paid_user',
    'admin'
    );

CREATE TABLE internal_user_accounts
(
    user_id                        UUID PRIMARY KEY REFERENCES user_accounts (id) ON DELETE CASCADE,
    password_hash                  TEXT           NOT NULL,
    two_factor_enabled             BOOLEAN        NOT NULL,
    two_factor_secret              TEXT,
    ban_timestamp                  TIMESTAMPTZ,
    ban_reason                     TEXT,
    failed_login_attempt_count     INTEGER        NOT NULL,
    last_failed_login_timestamp    TIMESTAMPTZ,
    account_locked_until_timestamp TIMESTAMPTZ,
    account_creation_timestamp     TIMESTAMPTZ    NOT NULL,
    last_password_change_timestamp TIMESTAMPTZ,
    last_login_timestamp           TIMESTAMPTZ,
    last_seen_timestamp            TIMESTAMPTZ,
    last_modified_by_user_id       UUID           REFERENCES user_accounts (id) ON DELETE SET NULL,
    last_modified_timestamp        TIMESTAMPTZ,
    user_role                      user_role_enum NOT NULL,
    schema_version                 INTEGER        NOT NULL
);

CREATE INDEX index_internal_user_accounts_last_modified_by_user_id ON internal_user_accounts (last_modified_by_user_id);
