CREATE TABLE users (
                       id                    BIGSERIAL PRIMARY KEY,
                       email                 VARCHAR(255) NOT NULL UNIQUE,
                       password              VARCHAR(255) NOT NULL,
                       name                  VARCHAR(255) NOT NULL,
                       role                  VARCHAR(50)  NOT NULL,
                       enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
                       account_non_locked    BOOLEAN      NOT NULL DEFAULT TRUE,
                       failed_login_attempts INT          NOT NULL DEFAULT 0,
                       locked_until          TIMESTAMP,
                       created_at            TIMESTAMP    NOT NULL,
                       updated_at            TIMESTAMP    NOT NULL
);

CREATE INDEX idx_users_email ON users (email);