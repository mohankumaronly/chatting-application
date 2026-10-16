ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_profile_avatar_public_id
    ON user_profiles(avatar_public_id)
    WHERE avatar_public_id IS NOT NULL;