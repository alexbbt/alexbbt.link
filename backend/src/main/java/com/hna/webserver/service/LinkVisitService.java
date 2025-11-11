package com.hna.webserver.service;

import com.hna.webserver.model.LinkVisit;
import com.hna.webserver.model.ShortLink;
import com.hna.webserver.repository.LinkVisitRepository;
import com.hna.webserver.repository.ShortLinkRepository;
import com.hna.webserver.util.RequestUtils;
import com.hna.webserver.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for logging and querying link visits.
 */
@Service
public class LinkVisitService {

    private static final Logger logger = LoggerFactory.getLogger(LinkVisitService.class);

    private final LinkVisitRepository visitRepository;
    private final ShortLinkRepository shortLinkRepository;

    public LinkVisitService(LinkVisitRepository visitRepository, ShortLinkRepository shortLinkRepository) {
        this.visitRepository = visitRepository;
        this.shortLinkRepository = shortLinkRepository;
    }

    /**
     * Logs a visit to a short link (called from async thread).
     * This method is designed to be non-blocking and should not delay the redirect.
     *
     * @param slug the slug of the visited link
     * @param ipAddress the client IP address
     * @param userAgent the user agent string
     * @param referrer the referrer URL
     * @param statusCode the HTTP status code returned
     */
    @Transactional
    public void logVisit(String slug, String ipAddress, String userAgent, String referrer, int statusCode) {
        try {
            logger.info("Logging visit for slug: {} with status: {}", slug, statusCode);
            Optional<ShortLink> shortLinkOpt = shortLinkRepository.findBySlugIgnoreCase(slug);

            LinkVisit visit = new LinkVisit();
            visit.setSlug(slug); // Always store the slug, even for 404s

            if (shortLinkOpt.isPresent()) {
                ShortLink shortLink = shortLinkOpt.get();
                visit.setShortLink(shortLink);
                visit.setCreatedBy(shortLink.getCreatedBy());
            } else {
                // For 404s, we still log the visit but without a ShortLink reference
                logger.debug("Logging visit for non-existent slug: {} (404)", slug);
                visit.setShortLink(null);
                visit.setCreatedBy(null);
            }

            visit.setIpAddress(ipAddress);
            visit.setUserAgent(userAgent);
            visit.setReferrer(referrer);
            visit.setStatusCode(statusCode);

            // Parse User-Agent for additional insights
            if (userAgent != null) {
                visit.setDeviceType(UserAgentParser.parseDeviceType(userAgent));
                visit.setBrowser(UserAgentParser.parseBrowser(userAgent));
                visit.setOperatingSystem(UserAgentParser.parseOperatingSystem(userAgent));
            }

            visitRepository.save(visit);
            logger.info("Successfully logged visit for slug: {} from IP: {}", slug, visit.getIpAddress());

        } catch (Exception e) {
            // Log error but don't throw - we don't want visit logging to break redirects
            logger.error("Failed to log visit for slug: {}", slug, e);
        }
    }

    /**
     * Gets visits for a specific short link with pagination.
     * Works for both existing links and 404s (using the slug field).
     *
     * @param slug the slug
     * @param pageable pagination parameters
     * @return page of visits
     */
    @Transactional(readOnly = true)
    public Page<LinkVisit> getVisitsForLink(String slug, Pageable pageable) {
        // Use the slug field directly (works for both existing links and 404s)
        return visitRepository.findBySlugOrderByCreatedAtDesc(slug, pageable);
    }

    /**
     * Gets visit count for a specific short link.
     * Works for both existing links and 404s (using the slug field).
     *
     * @param slug the slug
     * @return visit count
     */
    @Transactional(readOnly = true)
    public long getVisitCountForLink(String slug) {
        return visitRepository.countBySlug(slug);
    }

    /**
     * Gets visits for all links created by a user with pagination.
     *
     * @param username the username
     * @param pageable pagination parameters
     * @return page of visits
     */
    @Transactional(readOnly = true)
    public Page<LinkVisit> getVisitsForUser(String username, Pageable pageable) {
        return visitRepository.findByCreatedByOrderByCreatedAtDesc(username, pageable);
    }

    /**
     * Gets visit count for all links created by a user.
     *
     * @param username the username
     * @return visit count
     */
    @Transactional(readOnly = true)
    public long getVisitCountForUser(String username) {
        return visitRepository.countByCreatedBy(username);
    }

    /**
     * Gets all visits with pagination (admin only).
     * Manually handles pagination with fetch join to avoid LazyInitializationException.
     *
     * @param pageable pagination parameters
     * @return page of visits
     */
    @Transactional(readOnly = true)
    public Page<LinkVisit> getAllVisits(Pageable pageable) {
        // Get all visits with ShortLink eagerly loaded
        List<LinkVisit> allVisits = visitRepository.findAllWithShortLink();

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allVisits.size());
        List<LinkVisit> pagedVisits = start < allVisits.size()
            ? allVisits.subList(start, end)
            : List.of();

        return new org.springframework.data.domain.PageImpl<>(
            pagedVisits,
            pageable,
            allVisits.size()
        );
    }

    /**
     * Gets analytics statistics for a specific short link.
     *
     * @param slug the slug
     * @return map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsForLink(String slug) {
        Optional<ShortLink> shortLinkOpt = shortLinkRepository.findBySlugIgnoreCase(slug);
        if (shortLinkOpt.isEmpty()) {
            return new HashMap<>();
        }

        Long shortLinkId = shortLinkOpt.get().getId();
        long totalVisits = visitRepository.countByShortLinkId(shortLinkId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", totalVisits);
        stats.put("slug", slug);

        // Get visits by date
        List<Object[]> visitsByDate = visitRepository.getVisitStatsByDateForLink(shortLinkId);
        stats.put("visitsByDate", visitsByDate);

        // Get visits by country
        List<Object[]> visitsByCountry = visitRepository.getVisitStatsByCountryForLink(shortLinkId);
        stats.put("visitsByCountry", visitsByCountry);

        // Get visits by device type
        List<Object[]> visitsByDevice = visitRepository.getVisitStatsByDeviceTypeForLink(shortLinkId);
        stats.put("visitsByDevice", visitsByDevice);

        // Get visits by browser
        List<Object[]> visitsByBrowser = visitRepository.getVisitStatsByBrowserForLink(shortLinkId);
        stats.put("visitsByBrowser", visitsByBrowser);

        return stats;
    }

    /**
     * Gets analytics statistics for all links created by a user.
     *
     * @param username the username
     * @return map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsForUser(String username) {
        long totalVisits = visitRepository.countByCreatedBy(username);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", totalVisits);
        stats.put("username", username);

        return stats;
    }

    /**
     * Gets analytics statistics for all links (admin only).
     *
     * @return map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllAnalytics() {
        long totalVisits = visitRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", totalVisits);

        return stats;
    }
}
