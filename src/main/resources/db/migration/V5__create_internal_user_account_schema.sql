CREATE TYPE user_role_enum AS ENUM (
    'free_user',
    'paid_user',
    'admin'
    );

CREATE TABLE internal_user_accounts
(
    user_id                        UUID PRIMARY KEY
        REFERENCES user_accounts (id),
    password_hash                  TEXT                     NOT NULL,
    two_factor_enabled             BOOLEAN                  NOT NULL,
    two_factor_secret              TEXT,
    ban_timestamp                  TIMESTAMP WITH TIME ZONE,
    ban_reason                     TEXT,
    failed_login_attempt_count     INTEGER                  NOT NULL,
    last_failed_login_timestamp    TIMESTAMP WITH TIME ZONE,
    account_locked_until_timestamp TIMESTAMP WITH TIME ZONE,
    account_creation_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_password_change_timestamp TIMESTAMP WITH TIME ZONE,
    last_login_timestamp           TIMESTAMP WITH TIME ZONE,
    last_seen_timestamp            TIMESTAMP WITH TIME ZONE,
    registration_ip_address        INET                     NOT NULL,
    last_login_ip_address          INET,
    previous_login_ip_addresses    INET[]                   NOT NULL,
    known_device_tokens            UUID[]                   NOT NULL,
    last_modified_by_user_id       UUID
        REFERENCES user_accounts (id),
    last_modified_timestamp        TIMESTAMP WITH TIME ZONE,
    user_role                      user_role_enum           NOT NULL,
    user_account_schema_version    INTEGER                  NOT NULL
);
