CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE conversation_status_type AS ENUM (
    'active',
    'archived',
    'local',
    'temporary'
    );

CREATE TABLE conversations
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    title      TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    owner_id   UUID                     NOT NULL,
    status     conversation_status_type NOT NULL,
    version    INTEGER                  NOT NULL
);
