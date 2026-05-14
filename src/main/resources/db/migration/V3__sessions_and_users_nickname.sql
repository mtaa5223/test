CREATE TABLE sessions (
    id                      UUID PRIMARY KEY,
    user_id                 UUID NOT NULL REFERENCES users(id),
    device_id               TEXT NOT NULL,
    refresh_hash            TEXT NOT NULL UNIQUE,
    previous_refresh_hash   TEXT,
    issued_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    refresh_expires_at      TIMESTAMPTZ NOT NULL,
    last_seen_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at              TIMESTAMPTZ,
    revoked_reason          TEXT,
    user_agent              TEXT,
    ip                      INET
);

CREATE INDEX idx_sessions_user_active ON sessions (user_id) WHERE revoked_at IS NULL;
CREATE INDEX idx_sessions_prev_hash ON sessions (previous_refresh_hash) WHERE previous_refresh_hash IS NOT NULL;
CREATE INDEX idx_sessions_refresh_expires ON sessions (refresh_expires_at);

ALTER TABLE users
    ALTER COLUMN id DROP DEFAULT,
    ADD COLUMN nickname TEXT NOT NULL DEFAULT '';

UPDATE users SET nickname = 'Player_' || substr(md5(ugs_sub), 1, 6)
WHERE nickname = '';

ALTER TABLE users ALTER COLUMN nickname DROP DEFAULT;
