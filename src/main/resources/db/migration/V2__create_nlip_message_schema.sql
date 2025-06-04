CREATE TYPE allowed_format AS ENUM (
  'text',
  'token',
  'structured',
  'binary',
  'location',
  'generic'
);

CREATE TYPE message_type AS ENUM (
  'control'
);

CREATE TABLE nlip_message
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    conversation_id UUID           NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    ordinal         INTEGER        NOT NULL,
    message_type    message_type,
    format          allowed_format NOT NULL,
    subformat       TEXT           NOT NULL,
    content         TEXT           NOT NULL,
    label           TEXT
);

ALTER TABLE nlip_message
    ADD CONSTRAINT uniq_conversation_ordinal UNIQUE (conversation_id, ordinal);

CREATE TABLE nlip_submessage
(
    id         SERIAL PRIMARY KEY,
    message_id UUID           NOT NULL REFERENCES nlip_message (id) ON DELETE CASCADE,
    format     allowed_format NOT NULL,
    subformat  TEXT           NOT NULL,
    content    TEXT           NOT NULL,
    label      TEXT
);

CREATE INDEX idx_nlip_message_format ON nlip_message (format);
CREATE INDEX idx_nlip_message_conversation_ordinal ON nlip_message (conversation_id, ordinal);
CREATE INDEX idx_nlip_submessage_message_id ON nlip_submessage (message_id);
