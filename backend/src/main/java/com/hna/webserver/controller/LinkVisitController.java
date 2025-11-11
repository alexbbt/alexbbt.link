package com.hna.webserver.controller;

import com.hna.webserver.dto.LinkVisitResponse;
import com.hna.webserver.service.LinkVisitService;
import com.hna.webserver.service.ShortLinkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API controller for querying link visit logs and analytics.
 */
@RestController
@RequestMapping("/api/visits")
public class LinkVisitController {

    private final LinkVisitService linkVisitService;
    private final ShortLinkService shortLinkService;

    public LinkVisitController(LinkVisitService linkVisitService, ShortLinkService shortLinkService) {
        this.linkVisitService = linkVisitService;
        this.shortLinkService = shortLinkService;
    }

    /**
     * Gets visits for a specific short link with pagination.
     * Users can only view visits for their own links.
     * Admins can view visits for any link.
     *
     * @param slug the slug
     * @param page page number (0-indexed)
     * @param size page size
     * @param authentication the current authentication
     * @return paginated list of visits
     */
    @GetMapping("/link/{slug}")
    public ResponseEntity<Map<String, Object>> getVisitsForLink(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        // Check if user owns the link or is admin
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var shortLinkOpt = shortLinkService.getShortLinkBySlug(slug);
        if (shortLinkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var shortLink = shortLinkOpt.get();
        if (!isAdmin && (shortLink.getCreatedBy() == null || !shortLink.getCreatedBy().equals(username))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hna.webserver.model.LinkVisit> visits = linkVisitService.getVisitsForLink(slug, pageable);

        List<LinkVisitResponse> responses = visits.getContent().stream()
                .map(visit -> LinkVisitResponse.fromEntity(visit, shortLinkService.getBaseUrl()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("totalElements", visits.getTotalElements());
        result.put("totalPages", visits.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    /**
     * Gets visit count for a specific short link.
     *
     * @param slug the slug
     * @param authentication the current authentication
     * @return visit count
     */
    @GetMapping("/link/{slug}/count")
    public ResponseEntity<Map<String, Object>> getVisitCountForLink(
            @PathVariable String slug,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var shortLinkOpt = shortLinkService.getShortLinkBySlug(slug);
        if (shortLinkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var shortLink = shortLinkOpt.get();
        if (!isAdmin && (shortLink.getCreatedBy() == null || !shortLink.getCreatedBy().equals(username))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        long count = linkVisitService.getVisitCountForLink(slug);

        Map<String, Object> result = new HashMap<>();
        result.put("slug", slug);
        result.put("visitCount", count);

        return ResponseEntity.ok(result);
    }

    /**
     * Gets analytics for a specific short link.
     * Includes visits by date, country, device type, and browser.
     *
     * @param slug the slug
     * @param authentication the current authentication
     * @return analytics map
     */
    @GetMapping("/link/{slug}/analytics")
    public ResponseEntity<Map<String, Object>> getAnalyticsForLink(
            @PathVariable String slug,
            Authentication authentication) {

        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var shortLinkOpt = shortLinkService.getShortLinkBySlug(slug);
        if (shortLinkOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var shortLink = shortLinkOpt.get();
        if (!isAdmin && (shortLink.getCreatedBy() == null || !shortLink.getCreatedBy().equals(username))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> analytics = linkVisitService.getAnalyticsForLink(slug);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets visits for all links created by the current user with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param authentication the current authentication
     * @return paginated list of visits
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getVisitsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hna.webserver.model.LinkVisit> visits = linkVisitService.getVisitsForUser(username, pageable);

        List<LinkVisitResponse> responses = visits.getContent().stream()
                .map(visit -> LinkVisitResponse.fromEntity(visit, shortLinkService.getBaseUrl()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("totalElements", visits.getTotalElements());
        result.put("totalPages", visits.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    /**
     * Gets visit count for all links created by the current user.
     *
     * @param authentication the current authentication
     * @return visit count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getVisitCountForUser(Authentication authentication) {
        String username = authentication.getName();
        long count = linkVisitService.getVisitCountForUser(username);

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("visitCount", count);

        return ResponseEntity.ok(result);
    }

    /**
     * Gets analytics for all links created by the current user.
     *
     * @param authentication the current authentication
     * @return analytics map
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalyticsForUser(Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> analytics = linkVisitService.getAnalyticsForUser(username);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets all visits with pagination (admin only).
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated list of all visits
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllVisits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hna.webserver.model.LinkVisit> visits = linkVisitService.getAllVisits(pageable);

        List<LinkVisitResponse> responses = visits.getContent().stream()
                .map(visit -> LinkVisitResponse.fromEntity(visit, shortLinkService.getBaseUrl()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("totalElements", visits.getTotalElements());
        result.put("totalPages", visits.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    /**
     * Gets analytics for all links (admin only).
     *
     * @return analytics map
     */
    @GetMapping("/all/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAnalytics() {
        Map<String, Object> analytics = linkVisitService.getAllAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets all redirect requests (including 404s and other status codes) with pagination (admin only).
     * This shows all requests that hit the redirect controller, not just successful redirects.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated list of all redirect requests
     */
    @GetMapping("/redirects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRedirectRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hna.webserver.model.LinkVisit> visits = linkVisitService.getAllVisits(pageable);

        List<LinkVisitResponse> responses = visits.getContent().stream()
                .map(visit -> LinkVisitResponse.fromEntity(visit, shortLinkService.getBaseUrl()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("totalElements", visits.getTotalElements());
        result.put("totalPages", visits.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }
}
