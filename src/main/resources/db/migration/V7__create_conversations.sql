CREATE TYPE conversation_status_type_enum AS ENUM (
    'active',
    'archived',
    'local',
    'temporary'
    );

CREATE TABLE conversations
(
    id         UUID PRIMARY KEY                       DEFAULT gen_random_uuid(),
    title      TEXT                          NOT NULL,
    created_at TIMESTAMPTZ                   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ                   NOT NULL DEFAULT now(),
    owner_id   UUID                          NOT NULL REFERENCES user_accounts (id) ON DELETE CASCADE,
    status     conversation_status_type_enum NOT NULL,
    version    INTEGER                       NOT NULL
);

CREATE INDEX idx_conversations_owner_id ON conversations (owner_id);
