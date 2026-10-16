CREATE TABLE conversation_user_state (
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    archived_at TIMESTAMPTZ,
    pinned_at TIMESTAMPTZ,
    muted_until TIMESTAMPTZ,
    hidden_at TIMESTAMPTZ,
    unread_override BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (conversation_id, user_id)
);

CREATE INDEX idx_user_state_chat_list
    ON conversation_user_state(user_id)
    WHERE hidden_at IS NULL;
