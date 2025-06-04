CREATE TYPE conversation_status_type AS ENUM (
    'active',
    'archived',
    'local',
    'temporary'
    );

CREATE TABLE conversations
(
    id         UUID PRIMARY KEY,
    title      TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    owner_id   UUID                     NOT NULL,
    status     conversation_status_type NOT NULL,
    version    INTEGER                  NOT NULL
);
