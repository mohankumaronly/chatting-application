CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email_normalized VARCHAR(320) NOT NULL,
    email_verified_at TIMESTAMPTZ,
    date_of_birth DATE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email_normalized),
    CONSTRAINT chk_users_status CHECK (
        status IN (
            'PENDING_SETUP',
            'ACTIVE',
            'SUSPENDED',
            'PENDING_DELETION',
            'DEACTIVATED'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_users_email_normalized ON users(email_normalized);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
