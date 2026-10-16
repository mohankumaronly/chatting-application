package com.rockranger.chat.account.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deleted_user_identities")
public class DeletedUserIdentity {

    @Id
    private UUID id;

    @Column(name = "original_user_id", nullable = false)
    private UUID originalUserId;

    @Column(name = "email_hash", nullable = false, length = 255)
    private String emailHash;

    @Column(name = "username_normalized", length = 30)
    private String usernameNormalized;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt = Instant.now();

    public DeletedUserIdentity() {}

    public DeletedUserIdentity(UUID id, UUID originalUserId, String emailHash, String usernameNormalized) {
        this.id = id;
        this.originalUserId = originalUserId;
        this.emailHash = emailHash;
        this.usernameNormalized = usernameNormalized;
        this.deletedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOriginalUserId() { return originalUserId; }
    public void setOriginalUserId(UUID originalUserId) { this.originalUserId = originalUserId; }
    public String getEmailHash() { return emailHash; }
    public void setEmailHash(String emailHash) { this.emailHash = emailHash; }
    public String getUsernameNormalized() { return usernameNormalized; }
    public void setUsernameNormalized(String usernameNormalized) { this.usernameNormalized = usernameNormalized; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
