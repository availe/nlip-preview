CREATE TABLE nlip_submessage_attachments
(
    id                 BIGSERIAL PRIMARY KEY,
    nlip_submessage_id BIGINT      NOT NULL REFERENCES nlip_submessages (id) ON DELETE CASCADE,
    file_key           TEXT        NOT NULL,
    content_type       TEXT        NOT NULL,
    file_size_bytes    BIGINT      NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_nlip_submsg_attach_submsg_id ON nlip_submessage_attachments (nlip_submessage_id);
