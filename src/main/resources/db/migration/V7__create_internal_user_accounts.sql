CREATE TYPE user_role_enum AS ENUM (
    'free_user',
    'paid_user',
    'admin'
    );

CREATE TABLE internal_user_accounts
(
    user_id                        UUID PRIMARY KEY
        REFERENCES user_accounts (id)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,
    password_hash                  TEXT                     NOT NULL,
    two_factor_enabled             BOOLEAN                  NOT NULL DEFAULT FALSE,
    two_factor_secret              TEXT,
    ban_timestamp                  TIMESTAMP WITH TIME ZONE,
    ban_reason                     TEXT,
    failed_login_attempt_count     INTEGER                  NOT NULL DEFAULT 0,
    last_failed_login_timestamp    TIMESTAMP WITH TIME ZONE,
    account_locked_until_timestamp TIMESTAMP WITH TIME ZONE,
    account_creation_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_password_change_timestamp TIMESTAMP WITH TIME ZONE,
    last_login_timestamp           TIMESTAMP WITH TIME ZONE,
    last_seen_timestamp            TIMESTAMP WITH TIME ZONE,
    last_modified_by_user_id       UUID
                                                            REFERENCES user_accounts (id)
                                                                ON DELETE SET NULL,
    last_modified_timestamp        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    user_role                      user_role_enum           NOT NULL,
    schema_version                 INTEGER                  NOT NULL,
    CONSTRAINT ck_internal_user_password_hash_non_empty CHECK (password_hash <> '')
);

CREATE INDEX index_internal_user_accounts_last_modified_by_user_id
    ON internal_user_accounts (last_modified_by_user_id);

ALTER TABLE user_accounts
    ADD CONSTRAINT fk_user_accounts_requires_internal_user_account
        FOREIGN KEY (id)
            REFERENCES internal_user_accounts (user_id)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED;
