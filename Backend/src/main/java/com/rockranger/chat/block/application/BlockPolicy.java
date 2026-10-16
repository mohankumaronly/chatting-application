package com.rockranger.chat.block.application;

import java.util.Set;
import java.util.UUID;

public interface BlockPolicy {
    boolean isBlocked(UUID userId, UUID otherUserId);
    Set<UUID> filterBlocked(Set<UUID> candidateIds, UUID viewerId);
    boolean hasAnyBlock(UUID userId, UUID otherUserId);
}
