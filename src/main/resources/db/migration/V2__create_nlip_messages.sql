CREATE TABLE nlip_messages
(
    id           BIGSERIAL PRIMARY KEY,
    format       allowed_format_type_enum NOT NULL,
    subformat    TEXT                     NOT NULL,
    content_text TEXT,
    content_json JSONB,
    content_tsv  TSVECTOR GENERATED ALWAYS AS (to_tsvector('simple', coalesce(content_text, ''))) STORED,
    message_type message_type_enum,
    label        TEXT,
    CONSTRAINT ck_nlip_messages_content CHECK (
        (format = 'structured' AND content_json IS NOT NULL AND content_text IS NULL)
            OR (format <> 'structured' AND content_text IS NOT NULL AND content_json IS NULL)
        )
);

CREATE INDEX idx_nlip_messages_text_tsv ON nlip_messages USING GIN (content_tsv);
