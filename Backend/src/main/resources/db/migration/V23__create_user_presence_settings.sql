CREATE TABLE user_presence_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    last_seen_visibility VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    online_visibility VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    read_receipts_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    typing_visibility BOOLEAN NOT NULL DEFAULT TRUE,
    profile_photo_visibility VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_last_seen_visibility CHECK (
        last_seen_visibility IN ('EVERYONE', 'CONTACTS', 'NOBODY')
    )
);
