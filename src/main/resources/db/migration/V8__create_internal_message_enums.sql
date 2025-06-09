CREATE TYPE sender_type_enum AS ENUM (
    'user',
    'agent',
    'system'
    );

CREATE TABLE internal_messages
(
    id                UUID PRIMARY KEY,
    conversation_id   UUID             NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_type       sender_type_enum NOT NULL,
    sender_id         UUID             NOT NULL,
    nlip_message_id   BIGINT           NOT NULL REFERENCES nlip_messages (id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ      NOT NULL DEFAULT now(),
    parent_message_id UUID REFERENCES internal_messages (id) ON DELETE CASCADE,
    schema_version    INTEGER          NOT NULL,
    CONSTRAINT ck_internal_messages_no_self_parent CHECK (
        parent_message_id IS NULL OR parent_message_id <> id
        )
);

CREATE INDEX index_internal_messages_conversation_id ON internal_messages (conversation_id);
CREATE INDEX index_internal_messages_nlip_message_id ON internal_messages (nlip_message_id);
CREATE INDEX index_internal_messages_parent_message_id ON internal_messages (parent_message_id);
