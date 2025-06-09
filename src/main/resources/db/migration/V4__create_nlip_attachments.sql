CREATE TABLE nlip_attachments
(
    id                 BIGSERIAL PRIMARY KEY,
    nlip_message_id    BIGINT REFERENCES nlip_messages (id) ON DELETE CASCADE,
    nlip_submessage_id BIGINT REFERENCES nlip_submessages (id) ON DELETE CASCADE,
    file_key           TEXT        NOT NULL,
    content_type       TEXT        NOT NULL,
    file_size_bytes    BIGINT      NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    schema_version     INTEGER     NOT NULL,
    CONSTRAINT check_nlip_attachments_parent CHECK (
        (nlip_message_id IS NOT NULL AND nlip_submessage_id IS NULL)
            OR (nlip_message_id IS NULL AND nlip_submessage_id IS NOT NULL)
        ),
    CONSTRAINT uq_nlip_attachments_message_file
        UNIQUE (nlip_message_id, file_key),
    CONSTRAINT uq_nlip_attachments_submessage_file
        UNIQUE (nlip_submessage_id, file_key)
);

CREATE INDEX index_nlip_attachments_message_id ON nlip_attachments (nlip_message_id);
CREATE INDEX index_nlip_attachments_submessage_id ON nlip_attachments (nlip_submessage_id);
