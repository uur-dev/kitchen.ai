-- ============================================================
-- V1__init_schema.sql
-- Initial schema: client_devices, user_auth, auth_session
-- ============================================================

-- -----------------------------------------------------------
-- 1. client_devices
-- -----------------------------------------------------------
CREATE TABLE client_devices (
                                id         BIGSERIAL PRIMARY KEY,
                                device_type VARCHAR(50)             NOT NULL,
                                app_id     VARCHAR(255)             NOT NULL UNIQUE,
                                app_secret VARCHAR(255)             NOT NULL
);

-- -----------------------------------------------------------
-- 2. user_auth
-- -----------------------------------------------------------
CREATE TABLE user_auth (
                           id              BIGSERIAL PRIMARY KEY,
                           email           VARCHAR(255)          NOT NULL UNIQUE,
                           password        VARCHAR(255),                         -- nullable (social login)
                           fullname        VARCHAR(255),                         -- nullable
                           phone           VARCHAR(50),                          -- nullable
                           social_provider VARCHAR(50),                          -- nullable
                           social_id       VARCHAR(255),                         -- nullable
                           created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                           updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_auth_email          ON user_auth (email);
CREATE INDEX idx_user_auth_social         ON user_auth (social_provider, social_id);

-- -----------------------------------------------------------
-- 3. auth_session
-- -----------------------------------------------------------
CREATE TABLE auth_session (
                              id            BIGSERIAL PRIMARY KEY,
                              user_id       BIGINT                      NOT NULL REFERENCES user_auth (id) ON DELETE CASCADE,
                              device_type   VARCHAR(50)                 NOT NULL,
                              refresh_token VARCHAR(512)                NOT NULL UNIQUE,
                              fcm_token     VARCHAR(512),
                              expiry        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                              enabled       BOOLEAN                     NOT NULL DEFAULT TRUE,
                              created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                              updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auth_session_user_id       ON auth_session (user_id);
CREATE INDEX idx_auth_session_refresh_token ON auth_session (refresh_token);

-- -----------------------------------------------------------
-- Auto-update updated_at via trigger (optional but clean)
-- -----------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_auth_updated_at
    BEFORE UPDATE ON user_auth
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_auth_session_updated_at
    BEFORE UPDATE ON auth_session
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();