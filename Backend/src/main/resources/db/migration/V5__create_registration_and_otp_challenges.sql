CREATE TABLE IF NOT EXISTS registration_challenges (
    id UUID PRIMARY KEY,
    email_lookup VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    challenge_token_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reg_challenge_email ON registration_challenges(email_lookup);

CREATE TABLE IF NOT EXISTS otp_challenges (
    id UUID PRIMARY KEY,
    registration_id UUID REFERENCES registration_challenges(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    purpose VARCHAR(30) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_active
    ON otp_challenges(registration_id, purpose, expires_at)
    WHERE consumed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_otp_user_active
    ON otp_challenges(user_id, purpose, expires_at)
    WHERE consumed_at IS NULL;
