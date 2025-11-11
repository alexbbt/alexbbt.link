package com.hna.webserver.dto;

import com.hna.webserver.model.LinkVisit;
import java.time.LocalDateTime;

/**
 * DTO for link visit responses.
 */
public class LinkVisitResponse {

    private Long id;
    private String slug;
    private String shortUrl;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String userAgent;
    private String referrer;
    private Integer statusCode;
    private String countryCode;
    private String deviceType;
    private String browser;
    private String operatingSystem;

    public LinkVisitResponse() {
    }

    public LinkVisitResponse(Long id, String slug, String shortUrl, LocalDateTime createdAt,
                            String ipAddress, String userAgent, String referrer, Integer statusCode,
                            String countryCode, String deviceType, String browser, String operatingSystem) {
        this.id = id;
        this.slug = slug;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.statusCode = statusCode;
        this.countryCode = countryCode;
        this.deviceType = deviceType;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
    }

    public static LinkVisitResponse fromEntity(LinkVisit visit, String baseUrl) {
        // Use slug from visit entity (works for both existing links and 404s)
        // Prefer the slug field (denormalized), fall back to ShortLink if needed
        String slug = visit.getSlug();
        if (slug == null || slug.isEmpty()) {
            // Fallback for old records that might not have slug populated
            if (visit.getShortLink() != null) {
                try {
                    slug = visit.getShortLink().getSlug();
                } catch (Exception e) {
                    // LazyInitializationException or null - just use null slug
                    slug = null;
                }
            }
        }
        String shortUrl = slug != null && !slug.isEmpty() ? baseUrl + "/" + slug : null;

        return new LinkVisitResponse(
                visit.getId(),
                slug,
                shortUrl,
                visit.getCreatedAt(),
                visit.getIpAddress(),
                visit.getUserAgent(),
                visit.getReferrer(),
                visit.getStatusCode(),
                visit.getCountryCode(),
                visit.getDeviceType(),
                visit.getBrowser(),
                visit.getOperatingSystem()
        );
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
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
}
