package com.rockranger.chat.profile.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUsernameNormalized(String usernameNormalized);
    boolean existsByUsernameNormalized(String usernameNormalized);

    @Query("SELECT p FROM UserProfile p WHERE LOWER(p.usernameNormalized) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.usernameNormalized ASC")
    List<UserProfile> searchByUsername(@Param("query") String query, Pageable pageable);
}
