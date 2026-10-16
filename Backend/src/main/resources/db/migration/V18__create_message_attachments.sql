CREATE TABLE message_attachments (
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    attachment_id UUID NOT NULL REFERENCES attachments(id) ON DELETE CASCADE,
    display_order INT NOT NULL DEFAULT 0,

    PRIMARY KEY (message_id, attachment_id)
);

CREATE INDEX idx_message_attachments_attachment
    ON message_attachments(attachment_id);
