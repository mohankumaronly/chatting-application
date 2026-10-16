CREATE TABLE conversation_members (
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at TIMESTAMPTZ,
    last_read_message_id UUID,
    last_read_at TIMESTAMPTZ,

    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT chk_member_role CHECK (role IN ('MEMBER', 'ADMIN'))
);

CREATE INDEX idx_member_user_active
    ON conversation_members(user_id)
    WHERE left_at IS NULL;
