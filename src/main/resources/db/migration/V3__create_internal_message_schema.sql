CREATE TYPE sender_type_enum AS ENUM (
    'user',
    'agent',
    'system'
    );

CREATE TABLE internal_messages
(
    id                UUID PRIMARY KEY,
    conversation_id   UUID                     NOT NULL REFERENCES conversations (id),
    sender_type       sender_type_enum         NOT NULL,
    sender_id         UUID                     NOT NULL,
    nlip_request_id   BIGINT                   NOT NULL REFERENCES nlip_requests (id),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    parent_message_id UUID,
    version           INTEGER                  NOT NULL,
    CONSTRAINT internal_messages_parent_fk
        FOREIGN KEY (parent_message_id)
            REFERENCES internal_messages (id)
);
