package com.rockranger.chat.profile.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUsernameNormalized(String usernameNormalized);
    boolean existsByUsernameNormalized(String usernameNormalized);
}
