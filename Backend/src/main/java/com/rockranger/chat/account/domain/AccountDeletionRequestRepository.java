package com.rockranger.chat.account.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequest, UUID> {

    Optional<AccountDeletionRequest> findByUserIdAndStatus(UUID userId, AccountDeletionStatus status);

    Optional<AccountDeletionRequest> findByRecoveryTokenHashAndStatus(String recoveryTokenHash, AccountDeletionStatus status);

    @Query("SELECT r FROM AccountDeletionRequest r WHERE r.status = 'PENDING' AND r.recoverableUntil < :now ORDER BY r.recoverableUntil ASC")
    List<AccountDeletionRequest> findExpiredPendingRequests(@Param("now") Instant now, Pageable pageable);
}
