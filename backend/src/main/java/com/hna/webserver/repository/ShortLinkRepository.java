package com.hna.webserver.repository;

import com.hna.webserver.model.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT s FROM ShortLink s WHERE s.isActive = true ORDER BY s.updatedAt DESC")
    List<ShortLink> findActiveLinksOrderByUpdatedAtDesc();

    @Query("SELECT s FROM ShortLink s WHERE s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP) ORDER BY s.updatedAt DESC")
    List<ShortLink> findValidActiveLinksOrderByUpdatedAtDesc();

    List<ShortLink> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<ShortLink> findByCreatedByAndIsActiveOrderByCreatedAtDesc(String createdBy, Boolean isActive);
}
