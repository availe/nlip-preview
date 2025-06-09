CREATE TYPE platform_type_enum AS ENUM (
    'web',
    'ios',
    'android',
    'desktop'
    );

CREATE TYPE user_access_type_enum AS ENUM (
    'anonymous',
    'authenticated'
    );

CREATE TABLE connection_location_aggregates
(
    bucket_date       DATE                        NOT NULL,
    country_code      TEXT                        NOT NULL,
    region_code       TEXT,
    platform          platform_type_enum          NOT NULL,
    subscription_tier user_subscription_tier_enum NOT NULL,
    access_type       user_access_type_enum       NOT NULL,
    connection_count  BIGINT                      NOT NULL,
    schema_version    INTEGER                     NOT NULL,
    PRIMARY KEY (
                 bucket_date,
                 country_code,
                 region_code,
                 platform,
                 subscription_tier,
                 access_type
        )
);
