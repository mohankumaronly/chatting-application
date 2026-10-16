package com.rockranger.chat.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_identities", uniqueConstraints = {
        @UniqueConstraint(name = "uq_provider_subject", columnNames = {"provider", "provider_subject"})
})
public class UserIdentity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdentityProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UserIdentity() {}

    public UserIdentity(UUID id, UUID userId, IdentityProvider provider, String providerSubject, String passwordHash) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public IdentityProvider getProvider() { return provider; }
    public void setProvider(IdentityProvider provider) { this.provider = provider; }
    public String getProviderSubject() { return providerSubject; }
    public void setProviderSubject(String providerSubject) { this.providerSubject = providerSubject; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
