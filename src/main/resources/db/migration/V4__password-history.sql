CREATE TABLE password_history (
                                  id            BIGSERIAL PRIMARY KEY,
                                  user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  password_hash VARCHAR(255) NOT NULL,
                                  created_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_password_history_user_id ON password_history (user_id);