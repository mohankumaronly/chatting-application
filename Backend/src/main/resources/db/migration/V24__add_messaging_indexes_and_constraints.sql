-- Foreign key constraint for last_message_id in conversations
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_last_message
    FOREIGN KEY (last_message_id) REFERENCES messages(id) ON DELETE SET NULL;

-- Additional indexes for performance
CREATE INDEX IF NOT EXISTS idx_conversation_members_lookup
    ON conversation_members(conversation_id, user_id);

CREATE INDEX IF NOT EXISTS idx_message_receipts_lookup
    ON message_receipts(message_id, user_id);
