CREATE TABLE user_accounts
(
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username                    TEXT    NOT NULL,
    email_address               TEXT    NOT NULL,
    account_is_active           BOOLEAN NOT NULL,
    roles                       TEXT[]  NOT NULL,
    user_account_schema_version INTEGER NOT NULL
);