CREATE TYPE allowed_format_type AS ENUM (
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

CREATE TABLE nlip_requests
(
    id           BIGSERIAL PRIMARY KEY,
    format       allowed_format_type NOT NULL,
    subformat    TEXT                NOT NULL,
    content      TEXT                NOT NULL,
    message_type message_type,
    label        TEXT
);

CREATE TABLE nlip_submessages
(
    id              BIGSERIAL PRIMARY KEY,
    nlip_request_id BIGINT              NOT NULL REFERENCES nlip_requests (id),
    format          allowed_format_type NOT NULL,
    subformat       TEXT                NOT NULL,
    content         TEXT                NOT NULL,
    label           TEXT
);
