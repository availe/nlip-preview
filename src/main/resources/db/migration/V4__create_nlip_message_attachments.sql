CREATE TABLE nlip_message_attachments
(
    id              BIGSERIAL PRIMARY KEY,
    nlip_message_id BIGINT      NOT NULL REFERENCES nlip_messages (id) ON DELETE CASCADE,
    file_key        TEXT        NOT NULL,
    content_type    TEXT        NOT NULL,
    file_size_bytes BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_nlip_message_attach_msg_id ON nlip_message_attachments (nlip_message_id);
