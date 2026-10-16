CREATE TABLE IF NOT EXISTS user_blocks (
    id UUID PRIMARY KEY,
    blocker_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reason VARCHAR(255),

    CONSTRAINT uq_user_block UNIQUE (blocker_user_id, blocked_user_id),
    CONSTRAINT chk_no_self_block CHECK (blocker_user_id <> blocked_user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_blocks_blocked ON user_blocks(blocked_user_id);
CREATE INDEX IF NOT EXISTS idx_user_blocks_blocker ON user_blocks(blocker_user_id);
