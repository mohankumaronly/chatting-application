package com.rockranger.chat.presence.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserPresenceSettingsRepository extends JpaRepository<UserPresenceSettings, UUID> {
}
