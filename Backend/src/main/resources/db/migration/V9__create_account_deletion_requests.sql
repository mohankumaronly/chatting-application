CREATE TABLE IF NOT EXISTS account_deletion_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recoverable_until TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    reason VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    recovery_token_hash VARCHAR(255),
    recovery_token_expires_at TIMESTAMPTZ,

    CONSTRAINT chk_deletion_status CHECK (
        status IN ('PENDING', 'RECOVERED', 'COMPLETED', 'CANCELLED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_deletion_request
    ON account_deletion_requests(user_id)
    WHERE status = 'PENDING';
