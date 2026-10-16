CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    client_message_id UUID NOT NULL,
    sequence_number BIGINT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    content TEXT,
    reply_to_message_id UUID REFERENCES messages(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    edited_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    delete_scope VARCHAR(20),
    metadata JSONB,

    CONSTRAINT chk_message_type CHECK (
        message_type IN ('TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'FILE', 'GIF', 'SYSTEM')
    ),
    CONSTRAINT chk_delete_scope CHECK (
        delete_scope IS NULL OR delete_scope IN ('FOR_ME', 'FOR_EVERYONE')
    ),
    CONSTRAINT uq_sender_client_message UNIQUE (sender_id, client_message_id),
    CONSTRAINT uq_conversation_sequence UNIQUE (conversation_id, sequence_number)
);

CREATE INDEX idx_messages_history
    ON messages(conversation_id, sequence_number DESC);

CREATE INDEX idx_messages_sender
    ON messages(sender_id, created_at DESC);
