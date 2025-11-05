package com.hna.webserver.service;

import com.hna.webserver.model.ShortLink;
import com.hna.webserver.repository.ShortLinkRepository;
import com.hna.webserver.util.SlugGenerator;
import com.hna.webserver.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ShortLinkService {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkService.class);
    private static final int MAX_SLUG_GENERATION_ATTEMPTS = 10;

    private final ShortLinkRepository repository;
    private final String baseUrl;

    public ShortLinkService(ShortLinkRepository repository) {
        this.repository = repository;
        // In production, this should come from configuration
        this.baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:8080");
    }

    /**
     * Creates a new short link with optional custom slug or random slug.
     *
     * @param originalUrl the original URL to shorten
     * @param customSlug  optional custom slug (will generate random if not provided)
     * @param createdBy   username of the user creating the link
     * @return the created ShortLink
     * @throws IllegalArgumentException if URL is invalid or slug is already taken
     */
    @Transactional
    public ShortLink createShortLink(String originalUrl, String customSlug, String createdBy) {
        // Validate and normalize URL
        String normalizedUrl = UrlValidator.validateAndNormalize(originalUrl);
        if (normalizedUrl == null) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        String slug;
        if (customSlug != null && !customSlug.trim().isEmpty()) {
            // Use custom slug
            slug = customSlug.trim();
            if (!SlugGenerator.isValidSlug(slug)) {
                throw new IllegalArgumentException("Invalid slug format. Use alphanumeric characters, hyphens, or underscores.");
            }
            if (SlugGenerator.isReservedSlug(slug)) {
                throw new IllegalArgumentException("Slug is reserved and cannot be used");
            }
            if (repository.existsBySlugIgnoreCase(slug)) {
                throw new IllegalArgumentException("Slug already exists");
            }
        } else {
            // Generate random slug
            slug = generateUniqueSlug();
        }

        ShortLink shortLink = new ShortLink();
        shortLink.setSlug(slug);
        shortLink.setOriginalUrl(normalizedUrl);
        shortLink.setIsActive(true);
        shortLink.setCreatedBy(createdBy);

        ShortLink saved = repository.save(shortLink);
        logger.info("Created short link: {} -> {} (by: {})", slug, normalizedUrl, createdBy);
        return saved;
    }

    /**
     * Retrieves the original URL for a given slug (with caching).
     * Case-insensitive lookup.
     *
     * @param slug the slug to look up
     * @return the original URL if found and valid
     * @throws IllegalArgumentException if slug not found or link is invalid
     */
    @Cacheable(value = "shortLinks", key = "#slug.toLowerCase()")
    @Transactional(readOnly = true)
    public String getOriginalUrl(String slug) {
        Optional<ShortLink> optional = repository.findBySlugIgnoreCase(slug);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Short link not found");
        }

        ShortLink shortLink = optional.get();
        if (!shortLink.isValid()) {
            throw new IllegalArgumentException("Short link is inactive or expired");
        }

        return shortLink.getOriginalUrl();
    }

    /**
     * Increments the click count for a slug (async-friendly).
     * Case-insensitive lookup.
     *
     * @param slug the slug to increment clicks for
     */
    @Transactional
    @CacheEvict(value = "shortLinks", key = "#slug.toLowerCase()")
    public void incrementClickCount(String slug) {
        Optional<ShortLink> optional = repository.findBySlugIgnoreCase(slug);
        if (optional.isPresent()) {
            ShortLink shortLink = optional.get();
            shortLink.incrementClickCount();
            repository.save(shortLink);
            logger.debug("Incremented click count for slug: {}", slug);
        }
    }

    /**
     * Gets a short link by slug (for admin API).
     * Case-insensitive lookup.
     *
     * @param slug the slug
     * @return the ShortLink if found
     */
    @Transactional(readOnly = true)
    public Optional<ShortLink> getShortLinkBySlug(String slug) {
        return repository.findBySlugIgnoreCase(slug);
    }

    /**
     * Deletes a short link by slug.
     * Case-insensitive lookup.
     *
     * @param slug the slug to delete
     */
    @Transactional
    @CacheEvict(value = "shortLinks", key = "#slug.toLowerCase()")
    public void deleteShortLink(String slug) {
        Optional<ShortLink> optional = repository.findBySlugIgnoreCase(slug);
        if (optional.isPresent()) {
            repository.delete(optional.get());
            logger.info("Deleted short link: {}", slug);
        }
    }

    /**
     * Generates a unique random slug.
     *
     * @return a unique slug
     * @throws IllegalStateException if unable to generate unique slug after max attempts
     */
    private String generateUniqueSlug() {
        for (int attempt = 0; attempt < MAX_SLUG_GENERATION_ATTEMPTS; attempt++) {
            String slug = SlugGenerator.generateRandomSlug();
            if (!repository.existsBySlugIgnoreCase(slug)) {
                return slug;
            }
            logger.warn("Slug collision detected: {}, attempt {}", slug, attempt + 1);
        }
        throw new IllegalStateException("Unable to generate unique slug after " + MAX_SLUG_GENERATION_ATTEMPTS + " attempts");
    }

    /**
     * Gets all short links (for admin interface).
     *
     * @return list of all short links
     */
    @Transactional(readOnly = true)
    public java.util.List<ShortLink> getAllShortLinks() {
        return repository.findAll();
    }

    /**
     * Gets short links created by a specific user.
     *
     * @param username the username
     * @return list of short links created by the user
     */
    @Transactional(readOnly = true)
    public java.util.List<ShortLink> getShortLinksByUser(String username) {
        return repository.findByCreatedByOrderByCreatedAtDesc(username);
    }

    /**
     * Gets statistics about short links for a specific user.
     *
     * @param username the username
     * @return map of statistics
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStatisticsForUser(String username) {
        java.util.List<ShortLink> userLinks = repository.findByCreatedByOrderByCreatedAtDesc(username);
        long totalLinks = userLinks.size();
        long totalClicks = userLinks.stream()
                .mapToLong(ShortLink::getClickCount)
                .sum();
        long activeLinks = userLinks.stream()
                .filter(ShortLink::isValid)
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalLinks", totalLinks);
        stats.put("totalClicks", totalClicks);
        stats.put("activeLinks", activeLinks);
        stats.put("averageClicksPerLink", totalLinks > 0 ? (double) totalClicks / totalLinks : 0.0);
        return stats;
    }

    /**
     * Gets statistics about short links.
     *
     * @return map of statistics
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStatistics() {
        java.util.List<ShortLink> allLinks = repository.findAll();
        long totalLinks = allLinks.size();
        long totalClicks = allLinks.stream()
                .mapToLong(ShortLink::getClickCount)
                .sum();
        long activeLinks = allLinks.stream()
                .filter(ShortLink::isValid)
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalLinks", totalLinks);
        stats.put("totalClicks", totalClicks);
        stats.put("activeLinks", activeLinks);
        stats.put("averageClicksPerLink", totalLinks > 0 ? (double) totalClicks / totalLinks : 0.0);
        return stats;
    }

    /**
     * Gets the base URL for generating short URLs.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
