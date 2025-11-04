package com.hna.webserver.controller;

import com.hna.webserver.dto.CreateShortLinkRequest;
import com.hna.webserver.dto.ShortLinkResponse;
import com.hna.webserver.model.ShortLink;
import com.hna.webserver.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API controller for managing short links (admin interface).
 */
@RestController
@RequestMapping("/api/shortlinks")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    /**
     * Creates a new short link.
     *
     * @param request the create request with URL and optional slug
     * @return the created short link
     */
    @PostMapping
    public ResponseEntity<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        try {
            ShortLink shortLink = shortLinkService.createShortLink(
                    request.getUrl(),
                    request.getSlug()
            );

            ShortLinkResponse response = toResponse(shortLink);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets a short link by slug.
     *
     * @param slug the slug
     * @return the short link
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ShortLinkResponse> getShortLink(@PathVariable String slug) {
        return shortLinkService.getShortLinkBySlug(slug)
                .map(shortLink -> ResponseEntity.ok(toResponse(shortLink)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists all short links with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy sort field
     * @param sortDir sort direction
     * @return paginated list of short links
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getShortLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Get all links and sort them
        List<ShortLink> allLinks = shortLinkService.getAllShortLinks();

        // Simple manual sorting (for production, use Spring Data pagination in repository)
        allLinks.sort((a, b) -> {
            int comparison = 0;
            switch (sortBy) {
                case "createdAt":
                    comparison = a.getCreatedAt().compareTo(b.getCreatedAt());
                    break;
                case "clickCount":
                    comparison = a.getClickCount().compareTo(b.getClickCount());
                    break;
                case "slug":
                    comparison = a.getSlug().compareTo(b.getSlug());
                    break;
                default:
                    comparison = a.getCreatedAt().compareTo(b.getCreatedAt());
            }
            return sortDir.equalsIgnoreCase("asc") ? comparison : -comparison;
        });

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allLinks.size());
        List<ShortLink> pagedLinks = start < allLinks.size()
                ? allLinks.subList(Math.max(0, start), end)
                : List.of();

        List<ShortLinkResponse> responses = pagedLinks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("totalElements", allLinks.size());
        result.put("totalPages", (int) Math.ceil((double) allLinks.size() / size));
        result.put("currentPage", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    /**
     * Deletes a short link by slug.
     *
     * @param slug the slug to delete
     * @return no content
     */
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deleteShortLink(@PathVariable String slug) {
        shortLinkService.deleteShortLink(slug);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets statistics about short links.
     *
     * @return statistics map
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = shortLinkService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Converts ShortLink entity to response DTO.
     */
    private ShortLinkResponse toResponse(ShortLink shortLink) {
        String shortUrl = shortLinkService.getBaseUrl() + "/" + shortLink.getSlug();
        return new ShortLinkResponse(
                shortLink.getId(),
                shortLink.getSlug(),
                shortUrl,
                shortLink.getOriginalUrl(),
                shortLink.getClickCount(),
                shortLink.getCreatedAt(),
                shortLink.getUpdatedAt(),
                shortLink.getExpiresAt(),
                shortLink.getIsActive()
        );
    }
}
