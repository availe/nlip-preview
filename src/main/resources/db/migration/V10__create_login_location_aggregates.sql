CREATE TYPE platform_type_enum AS ENUM (
    'web',
    'ios',
    'android',
    'desktop'
    );

CREATE TABLE login_location_aggregates
(
    bucket_date  DATE               NOT NULL,
    country_code TEXT               NOT NULL,
    region_code  TEXT,
    platform     platform_type_enum NOT NULL,
    login_count  BIGINT             NOT NULL,
    PRIMARY KEY (bucket_date, country_code, region_code, platform)
);
