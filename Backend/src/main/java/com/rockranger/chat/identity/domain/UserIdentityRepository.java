package com.rockranger.chat.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {
    Optional<UserIdentity> findByUserIdAndProvider(UUID userId, IdentityProvider provider);
    Optional<UserIdentity> findByProviderAndProviderSubject(IdentityProvider provider, String providerSubject);
    List<UserIdentity> findAllByUserId(UUID userId);
    long countByUserId(UUID userId);
    void deleteByUserIdAndProvider(UUID userId, IdentityProvider provider);
}
