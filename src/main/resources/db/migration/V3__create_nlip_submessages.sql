CREATE TABLE nlip_submessages
(
    id              BIGSERIAL PRIMARY KEY,
    nlip_message_id BIGINT                   NOT NULL REFERENCES nlip_messages (id) ON DELETE CASCADE,
    position        INTEGER                  NOT NULL,
    format          allowed_format_type_enum NOT NULL,
    subformat       TEXT                     NOT NULL,
    content_text    TEXT,
    content_json    JSONB,
    created_at      TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ              NOT NULL DEFAULT now(),
    schema_version  INTEGER                  NOT NULL,
    label           TEXT,
    CONSTRAINT check_nlip_submessages_content CHECK (
        (format = 'structured' AND content_json IS NOT NULL AND content_text IS NULL)
            OR (format <> 'structured' AND content_text IS NOT NULL AND content_json IS NULL)
        ),
    CONSTRAINT unique_nlip_submessages_message_position UNIQUE (nlip_message_id, position)
);

CREATE INDEX index_nlip_submessages_message_id ON nlip_submessages (nlip_message_id);
