CREATE TABLE password_reset_tokens (
                                       id         BIGSERIAL    PRIMARY KEY,
                                       token      VARCHAR(512) NOT NULL UNIQUE,
                                       user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       expires_at TIMESTAMP    NOT NULL,
                                       used       BOOLEAN      NOT NULL DEFAULT FALSE,
                                       created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);