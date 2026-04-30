CREATE TABLE consumed_ugs_jti (
    jti         TEXT PRIMARY KEY,
    sub         TEXT NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_consumed_ugs_jti_expires ON consumed_ugs_jti (expires_at);
