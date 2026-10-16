CREATE TABLE IF NOT EXISTS email_outbox (
    id UUID PRIMARY KEY,
    recipient VARCHAR(320) NOT NULL,
    template VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ,

    CONSTRAINT chk_outbox_status CHECK (
        status IN ('PENDING', 'SENT', 'FAILED', 'DEAD_LETTER')
    )
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON email_outbox(created_at)
    WHERE status = 'PENDING';
