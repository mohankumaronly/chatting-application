CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_name VARCHAR(150),
    client_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_session_id UUID REFERENCES user_sessions(id),

    CONSTRAINT chk_client_type CHECK (
        client_type IN ('WEB', 'ANDROID', 'IOS', 'DESKTOP')
    )
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_active
    ON user_sessions(user_id, expires_at)
    WHERE revoked_at IS NULL;
