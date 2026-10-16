package com.rockranger.chat.block.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {

    Optional<UserBlock> findByBlockerUserIdAndBlockedUserId(UUID blockerUserId, UUID blockedUserId);

    boolean existsByBlockerUserIdAndBlockedUserId(UUID blockerUserId, UUID blockedUserId);

    Page<UserBlock> findAllByBlockerUserId(UUID blockerUserId, Pageable pageable);

    void deleteByBlockerUserIdAndBlockedUserId(UUID blockerUserId, UUID blockedUserId);
}
