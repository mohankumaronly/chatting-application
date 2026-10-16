CREATE TABLE message_receipts (
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    delivered_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,

    PRIMARY KEY (message_id, user_id)
);

CREATE INDEX idx_receipts_unread
    ON message_receipts(user_id)
    WHERE read_at IS NULL;
