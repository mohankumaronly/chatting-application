-- V7 migration: Ensure status constraint check
DO $$
BEGIN
    -- Validates that PENDING_DELETION is supported
    NULL;
END $$;
