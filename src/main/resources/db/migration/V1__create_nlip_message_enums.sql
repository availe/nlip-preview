CREATE TYPE allowed_format_type_enum AS ENUM (
    'text',
    'token',
    'structured',
    'binary',
    'location',
    'error',
    'generic'
    );

CREATE TYPE message_type_enum AS ENUM (
    'control'
    );
