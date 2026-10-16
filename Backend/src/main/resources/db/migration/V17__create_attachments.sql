CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    storage_provider VARCHAR(30) NOT NULL DEFAULT 'CLOUDINARY',
    storage_key VARCHAR(500) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    checksum VARCHAR(128),
    width INT,
    height INT,
    duration_ms INT,
    upload_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_upload_status CHECK (
        upload_status IN ('PENDING', 'UPLOADED', 'READY', 'REJECTED')
    ),
    CONSTRAINT chk_resource_type CHECK (
        resource_type IN ('IMAGE', 'VIDEO', 'RAW', 'AUDIO')
    )
);

CREATE INDEX idx_attachments_owner ON attachments(owner_id, created_at DESC);
CREATE INDEX idx_attachments_status ON attachments(upload_status)
    WHERE upload_status IN ('PENDING', 'UPLOADED');
