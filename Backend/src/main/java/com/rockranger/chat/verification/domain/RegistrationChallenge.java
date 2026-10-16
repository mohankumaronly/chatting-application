package com.rockranger.chat.verification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "registration_challenges")
public class RegistrationChallenge {

    @Id
    private UUID id;

    @Column(name = "email_lookup", nullable = false, length = 255)
    private String emailLookup;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "challenge_token_hash", nullable = false, length = 255)
    private String challengeTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationChallengeStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public RegistrationChallenge() {}

    public RegistrationChallenge(UUID id, String emailLookup, LocalDate dateOfBirth, String challengeTokenHash, Instant expiresAt) {
        this.id = id;
        this.emailLookup = emailLookup;
        this.dateOfBirth = dateOfBirth;
        this.challengeTokenHash = challengeTokenHash;
        this.status = RegistrationChallengeStatus.OTP_SENT;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmailLookup() { return emailLookup; }
    public void setEmailLookup(String emailLookup) { this.emailLookup = emailLookup; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getChallengeTokenHash() { return challengeTokenHash; }
    public void setChallengeTokenHash(String challengeTokenHash) { this.challengeTokenHash = challengeTokenHash; }
    public RegistrationChallengeStatus getStatus() { return status; }
    public void setStatus(RegistrationChallengeStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
