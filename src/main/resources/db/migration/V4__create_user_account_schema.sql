CREATE TABLE user_accounts
(
    id                             UUID PRIMARY KEY,
    username                       TEXT                     NOT NULL,
    email_address                  TEXT                     NOT NULL,
    password_hash                  TEXT                     NOT NULL,
    two_factor_enabled             BOOLEAN                  NOT NULL,
    two_factor_secret              TEXT,
    account_is_active              BOOLEAN                  NOT NULL,
    ban_timestamp                  TIMESTAMP WITH TIME ZONE,
    ban_reason                     TEXT,
    failed_login_attempt_count     INTEGER                  NOT NULL,
    last_failed_login_timestamp    TIMESTAMP WITH TIME ZONE,
    account_locked_until_timestamp TIMESTAMP WITH TIME ZONE,
    account_creation_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_password_change_timestamp TIMESTAMP WITH TIME ZONE,
    last_login_timestamp           TIMESTAMP WITH TIME ZONE,
    last_seen_timestamp            TIMESTAMP WITH TIME ZONE,
    registration_ip_address        TEXT                     NOT NULL,
    last_login_ip_address          TEXT,
    previous_login_ip_addresses    TEXT[]                   NOT NULL,
    known_device_tokens            TEXT[]                   NOT NULL,
    roles                          TEXT[]                   NOT NULL,
    last_modified_by_user_id       UUID,
    last_modified_timestamp        TIMESTAMP WITH TIME ZONE,
    user_account_version           INTEGER                  NOT NULL,
    CONSTRAINT fk_user_last_modified_by
        FOREIGN KEY (last_modified_by_user_id)
            REFERENCES user_accounts (id)
);

CREATE UNIQUE INDEX ux_user_accounts_username ON user_accounts (username);
CREATE UNIQUE INDEX ux_user_accounts_email_address ON user_accounts (email_address);
