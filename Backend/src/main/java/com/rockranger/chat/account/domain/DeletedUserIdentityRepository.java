package com.rockranger.chat.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeletedUserIdentityRepository extends JpaRepository<DeletedUserIdentity, UUID> {
    Optional<DeletedUserIdentity> findByEmailHash(String emailHash);
    boolean existsByEmailHash(String emailHash);
    boolean existsByUsernameNormalized(String usernameNormalized);
}
