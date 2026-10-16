package com.rockranger.chat.verification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistrationChallengeRepository extends JpaRepository<RegistrationChallenge, UUID> {
    Optional<RegistrationChallenge> findByIdAndStatus(UUID id, RegistrationChallengeStatus status);
}
