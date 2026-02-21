CREATE TABLE security_events (
                                 id         BIGSERIAL    PRIMARY KEY,
                                 email      VARCHAR(255) NOT NULL,
                                 type       VARCHAR(50)  NOT NULL,
                                 ip_address VARCHAR(45),
                                 created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_security_events_email ON security_events (email);
CREATE INDEX idx_security_events_created_at ON security_events (created_at);