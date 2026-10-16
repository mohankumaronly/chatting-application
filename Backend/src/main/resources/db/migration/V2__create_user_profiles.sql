CREATE TABLE IF NOT EXISTS user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    username VARCHAR(30),
    username_normalized VARCHAR(30),
    bio VARCHAR(500),
    avatar_public_id VARCHAR(255),
    username_changed_at TIMESTAMPTZ,
    profile_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_profile_username UNIQUE (username_normalized)
);

CREATE INDEX IF NOT EXISTS idx_profile_username_norm ON user_profiles(username_normalized);
