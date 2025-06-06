CREATE TYPE user_subscription_tier_enum AS ENUM (
    'standard',
    'byok',
    'enterprise'
    );

CREATE TABLE user_accounts
(
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username                    TEXT                        NOT NULL,
    email_address               TEXT                        NOT NULL UNIQUE,
    account_is_active           BOOLEAN                     NOT NULL,
    user_subscription_tier      user_subscription_tier_enum NOT NULL,
    user_account_schema_version INTEGER                     NOT NULL
);