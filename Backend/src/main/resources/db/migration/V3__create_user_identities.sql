CREATE TABLE IF NOT EXISTS user_identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    password_hash TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT chk_identity_provider CHECK (provider IN ('LOCAL', 'GOOGLE'))
);

CREATE INDEX IF NOT EXISTS idx_user_identities_user_id ON user_identities(user_id);
