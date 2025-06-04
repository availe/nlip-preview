CREATE TYPE conversation_status AS ENUM (
    'active',
    'archived',
    'local',
    'temporary'
);

CREATE TYPE sender_type AS ENUM (
    'user',
    'agent',
    'system'
);

CREATE TABLE user_account
(
    id            UUID PRIMARY KEY,
    username      VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    last_login_at TIMESTAMPTZ  NOT NULL,
    is_online     BOOLEAN      NOT NULL DEFAULT TRUE,
    version       INT          NOT NULL
);

CREATE UNIQUE INDEX uq_user_account_username ON user_account (username);
CREATE UNIQUE INDEX uq_user_account_email ON user_account (email);

CREATE TABLE conversation
(
    id         UUID PRIMARY KEY,
    title      TEXT                NOT NULL,
    created_at TIMESTAMPTZ         NOT NULL,
    updated_at TIMESTAMPTZ         NOT NULL,
    owner      UUID                NOT NULL REFERENCES user_account (id),
    status     conversation_status NOT NULL,
    version    INT                 NOT NULL
);

CREATE TABLE internal_message
(
    id                UUID PRIMARY KEY,
    conversation_id   UUID        NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    sender_type       sender_type NOT NULL,
    sender_id         UUID        NOT NULL,
    nlip_message      JSONB       NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    parent_message_id UUID        REFERENCES internal_message (id) ON DELETE SET NULL,
    version           INT         NOT NULL
);

CREATE INDEX idx_internal_message_conversation_id ON internal_message (conversation_id);
CREATE INDEX idx_internal_message_parent_message_id ON internal_message (parent_message_id);
