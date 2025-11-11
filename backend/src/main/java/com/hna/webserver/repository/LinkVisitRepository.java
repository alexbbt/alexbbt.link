package com.hna.webserver.repository;

import com.hna.webserver.model.LinkVisit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LinkVisitRepository extends JpaRepository<LinkVisit, Long> {

    /**
     * Finds all visits with ShortLink eagerly loaded to avoid LazyInitializationException.
     * Note: Spring Data JPA doesn't support fetch joins with Pageable directly,
     * so we'll use a workaround in the service.
     */
    @Query("SELECT DISTINCT v FROM LinkVisit v LEFT JOIN FETCH v.shortLink ORDER BY v.createdAt DESC")
    List<LinkVisit> findAllWithShortLink();

    /**
     * Finds all visits for a specific short link, ordered by most recent first.
     */
    Page<LinkVisit> findByShortLinkIdOrderByCreatedAtDesc(Long shortLinkId, Pageable pageable);

    /**
     * Counts visits for a specific short link.
     */
    long countByShortLinkId(Long shortLinkId);

    /**
     * Finds all visits for links created by a specific user.
     */
    @Query("SELECT v FROM LinkVisit v WHERE v.createdBy = :username ORDER BY v.createdAt DESC")
    Page<LinkVisit> findByCreatedByOrderByCreatedAtDesc(@Param("username") String username, Pageable pageable);

    /**
     * Counts all visits for links created by a specific user.
     */
    @Query("SELECT COUNT(v) FROM LinkVisit v WHERE v.createdBy = :username")
    long countByCreatedBy(@Param("username") String username);

    /**
     * Finds all visits for a specific short link within a date range.
     */
    @Query("SELECT v FROM LinkVisit v WHERE v.shortLink.id = :shortLinkId " +
           "AND v.createdAt BETWEEN :startDate AND :endDate ORDER BY v.createdAt DESC")
    List<LinkVisit> findByShortLinkIdAndDateRange(
            @Param("shortLinkId") Long shortLinkId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds all visits for links created by a user within a date range.
     */
    @Query("SELECT v FROM LinkVisit v WHERE v.createdBy = :username " +
           "AND v.createdAt BETWEEN :startDate AND :endDate ORDER BY v.createdAt DESC")
    List<LinkVisit> findByCreatedByAndDateRange(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Gets visit statistics grouped by date for a specific short link.
     * Uses CAST to DATE for PostgreSQL compatibility.
     */
    @Query(value = "SELECT CAST(v.created_at AS DATE) as visitDate, COUNT(v) as visitCount " +
           "FROM link_visits v WHERE v.short_link_id = :shortLinkId " +
           "GROUP BY CAST(v.created_at AS DATE) ORDER BY visitDate DESC", nativeQuery = true)
    List<Object[]> getVisitStatsByDateForLink(@Param("shortLinkId") Long shortLinkId);

    /**
     * Finds visits by slug (works for both existing links and 404s).
     */
    @Query("SELECT v FROM LinkVisit v WHERE v.slug = :slug ORDER BY v.createdAt DESC")
    Page<LinkVisit> findBySlugOrderByCreatedAtDesc(@Param("slug") String slug, Pageable pageable);

    /**
     * Counts visits by slug (works for both existing links and 404s).
     */
    long countBySlug(String slug);

    /**
     * Gets visit statistics grouped by country for a specific short link.
     */
    @Query("SELECT v.countryCode, COUNT(v) as visitCount " +
           "FROM LinkVisit v WHERE v.shortLink.id = :shortLinkId AND v.countryCode IS NOT NULL " +
           "GROUP BY v.countryCode ORDER BY visitCount DESC")
    List<Object[]> getVisitStatsByCountryForLink(@Param("shortLinkId") Long shortLinkId);

    /**
     * Gets visit statistics grouped by device type for a specific short link.
     */
    @Query("SELECT v.deviceType, COUNT(v) as visitCount " +
           "FROM LinkVisit v WHERE v.shortLink.id = :shortLinkId AND v.deviceType IS NOT NULL " +
           "GROUP BY v.deviceType ORDER BY visitCount DESC")
    List<Object[]> getVisitStatsByDeviceTypeForLink(@Param("shortLinkId") Long shortLinkId);

    /**
     * Gets visit statistics grouped by browser for a specific short link.
     */
    @Query("SELECT v.browser, COUNT(v) as visitCount " +
           "FROM LinkVisit v WHERE v.shortLink.id = :shortLinkId AND v.browser IS NOT NULL " +
           "GROUP BY v.browser ORDER BY visitCount DESC")
    List<Object[]> getVisitStatsByBrowserForLink(@Param("shortLinkId") Long shortLinkId);
}
