CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    direct_pair_key VARCHAR(80),
    next_sequence BIGINT NOT NULL DEFAULT 0,
    last_message_id UUID,
    last_message_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_conversation_type CHECK (type IN ('DIRECT', 'GROUP'))
);

CREATE UNIQUE INDEX uq_conversation_direct_pair
    ON conversations(direct_pair_key)
    WHERE type = 'DIRECT' AND direct_pair_key IS NOT NULL;

CREATE INDEX idx_conversation_last_message
    ON conversations(last_message_at DESC NULLS LAST);
