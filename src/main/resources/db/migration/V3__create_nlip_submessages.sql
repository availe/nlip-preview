CREATE TABLE nlip_submessages
(
    id              BIGSERIAL PRIMARY KEY,
    nlip_message_id BIGINT                   NOT NULL REFERENCES nlip_messages (id) ON DELETE CASCADE,
    position        INTEGER                  NOT NULL,
    format          allowed_format_type_enum NOT NULL,
    subformat       TEXT                     NOT NULL,
    content_text    TEXT,
    content_json    JSONB,
    content_tsv     TSVECTOR GENERATED ALWAYS AS (to_tsvector('simple', coalesce(content_text, ''))) STORED,
    label           TEXT,
    CONSTRAINT ck_nlip_submessages_content CHECK (
        (format = 'structured' AND content_json IS NOT NULL AND content_text IS NULL)
            OR (format <> 'structured' AND content_text IS NOT NULL AND content_json IS NULL)
        )
);

CREATE INDEX idx_nlip_submsgs_msg_pos ON nlip_submessages (nlip_message_id, position);
CREATE INDEX idx_nlip_submessages_text_tsv ON nlip_submessages USING GIN (content_tsv);
