package com.hna.webserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a visit/click on a short link.
 * Stores comprehensive information about each visit for analytics purposes.
 */
@Entity
@Table(name = "link_visits", indexes = {
    @Index(name = "idx_link_visit_short_link_id", columnList = "short_link_id"),
    @Index(name = "idx_link_visit_created_at", columnList = "created_at"),
    @Index(name = "idx_link_visit_ip", columnList = "ip_address"),
    @Index(name = "idx_link_visit_created_by", columnList = "created_by"),
    @Index(name = "idx_link_visit_slug", columnList = "slug")
})
public class LinkVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The short link that was visited.
     * Nullable to allow logging 404s for non-existent slugs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_link_id", nullable = true)
    private ShortLink shortLink;

    /**
     * The slug that was requested (denormalized for 404s and easier querying).
     * This allows us to log visits even when the slug doesn't exist.
     */
    @Column(name = "slug", length = 50)
    private String slug;

    /**
     * Timestamp when the visit occurred.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * IP address of the visitor.
     * Supports IPv4 and IPv6 addresses.
     * For proxied requests, this should be the client IP (from X-Forwarded-For).
     */
    @Column(name = "ip_address", length = 45) // IPv6 max length is 45
    private String ipAddress;

    /**
     * User-Agent string from the HTTP request.
     * Contains browser, OS, and device information.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Referrer URL - where the visitor came from.
     * null if direct visit (no referrer).
     */
    @Column(columnDefinition = "TEXT")
    private String referrer;

    /**
     * HTTP status code returned (e.g., 302 for redirect, 404 for not found).
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * Country code (ISO 3166-1 alpha-2) if available from IP geolocation.
     * Can be populated later by a background job.
     */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /**
     * Device type inferred from User-Agent (mobile, desktop, tablet, bot).
     * Can be populated by parsing User-Agent.
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * Browser name inferred from User-Agent (Chrome, Firefox, Safari, etc.).
     * Can be populated by parsing User-Agent.
     */
    @Column(name = "browser", length = 50)
    private String browser;

    /**
     * Operating system inferred from User-Agent (Windows, macOS, Linux, iOS, Android).
     * Can be populated by parsing User-Agent.
     */
    @Column(name = "operating_system", length = 50)
    private String operatingSystem;

    /**
     * Username of the link creator (denormalized for easier querying).
     * This allows filtering visits by link owner without joining.
     */
    @Column(name = "created_by", length = 255)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShortLink getShortLink() {
        return shortLink;
    }

    public void setShortLink(ShortLink shortLink) {
        this.shortLink = shortLink;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
