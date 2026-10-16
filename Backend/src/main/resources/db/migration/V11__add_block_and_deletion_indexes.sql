CREATE INDEX IF NOT EXISTS idx_account_deletion_pending
    ON account_deletion_requests(recoverable_until)
    WHERE status = 'PENDING';
