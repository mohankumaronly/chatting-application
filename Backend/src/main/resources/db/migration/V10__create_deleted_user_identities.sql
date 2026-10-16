CREATE TABLE IF NOT EXISTS deleted_user_identities (
    id UUID PRIMARY KEY,
    original_user_id UUID NOT NULL,
    email_hash VARCHAR(255) NOT NULL,
    username_normalized VARCHAR(30),
    deleted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_deleted_identity_email_hash
    ON deleted_user_identities(email_hash);
