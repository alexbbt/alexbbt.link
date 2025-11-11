package com.hna.webserver.controller;

import com.hna.webserver.service.LinkVisitService;
import com.hna.webserver.service.ShortLinkService;
import com.hna.webserver.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

/**
 * Controller for handling root-level redirects (short links).
 * Handles routes like /{slug} to redirect to original URLs.
 * Set to LOWEST_PRECEDENCE to allow static resources to be served first.
 */
@RestController
@Order(Ordered.LOWEST_PRECEDENCE)
public class RedirectController {

    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    /**
     * Reserved paths that should not be handled as short links.
     */
    private static final Set<String> RESERVED_PATHS = Set.of(
            "admin", "api", "_next", "favicon.ico"
    );

    private final ShortLinkService shortLinkService;
    private final LinkVisitService linkVisitService;

    public RedirectController(ShortLinkService shortLinkService, LinkVisitService linkVisitService) {
        this.shortLinkService = shortLinkService;
        this.linkVisitService = linkVisitService;
    }

    /**
     * Handles root-level redirects for short links.
     * This catches all routes that aren't reserved paths or static files.
     * Uses regex to exclude paths containing dots (file extensions).
     * Note: This mapping excludes reserved paths (admin, api, _next) and files
     * which are handled by static resources or other controllers.
     *
     * @param slug the slug to redirect
     * @param request the HTTP request
     * @param response HTTP response to set redirect
     * @throws IOException if redirect fails
     */
    @GetMapping("/{slug:[^.]+}")
    public void redirect(@PathVariable String slug, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Skip reserved paths - these should never reach here due to regex pattern and static resources
        // But handle gracefully just in case
        if (RESERVED_PATHS.contains(slug.toLowerCase())) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        int statusCode = HttpStatus.FOUND.value(); // 302
        final int finalStatusCode = statusCode; // Capture for lambda

        // Extract request data before starting thread (HttpServletRequest is not thread-safe)
        final String clientIp = RequestUtils.getClientIpAddress(request);
        final String userAgent = RequestUtils.getUserAgent(request);
        final String referrer = RequestUtils.getReferrer(request);

        try {
            // Get original URL (with caching)
            String originalUrl = shortLinkService.getOriginalUrl(slug);

            // Increment click count asynchronously (fire and forget)
            new Thread(() -> {
                try {
                    shortLinkService.incrementClickCount(slug);
                } catch (Exception e) {
                    logger.error("Failed to increment click count for slug: {}", slug, e);
                }
            }).start();

            // Log visit asynchronously (fire and forget)
            // Using a thread to ensure it doesn't block the redirect
            // Extract request data first since HttpServletRequest is not thread-safe
            final String finalSlug = slug;
            new Thread(() -> {
                try {
                    linkVisitService.logVisit(finalSlug, clientIp, userAgent, referrer, finalStatusCode);
                } catch (Exception e) {
                    logger.error("Failed to log visit in thread for slug: {}", finalSlug, e);
                }
            }).start();

            // Perform redirect
            response.sendRedirect(originalUrl);
            logger.debug("Redirected slug {} to {}", slug, originalUrl);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid slug or link not found: {}", slug);
            final int notFoundStatusCode = HttpStatus.NOT_FOUND.value();
            response.setStatus(notFoundStatusCode);
            response.getWriter().write("Short link not found");
            // Log failed visit attempt
            final String notFoundSlug = slug;
            final String notFoundIp = RequestUtils.getClientIpAddress(request);
            final String notFoundUserAgent = RequestUtils.getUserAgent(request);
            final String notFoundReferrer = RequestUtils.getReferrer(request);
            new Thread(() -> {
                try {
                    linkVisitService.logVisit(notFoundSlug, notFoundIp, notFoundUserAgent, notFoundReferrer, notFoundStatusCode);
                } catch (Exception ex) {
                    logger.error("Failed to log visit in thread for slug: {}", notFoundSlug, ex);
                }
            }).start();
        } catch (Exception e) {
            logger.error("Error redirecting slug: {}", slug, e);
            final int errorStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            response.setStatus(errorStatusCode);
            response.getWriter().write("Internal server error");
            // Log error visit
            final String errorSlug = slug;
            final String errorIp = RequestUtils.getClientIpAddress(request);
            final String errorUserAgent = RequestUtils.getUserAgent(request);
            final String errorReferrer = RequestUtils.getReferrer(request);
            new Thread(() -> {
                try {
                    linkVisitService.logVisit(errorSlug, errorIp, errorUserAgent, errorReferrer, errorStatusCode);
                } catch (Exception ex) {
                    logger.error("Failed to log visit in thread for slug: {}", errorSlug, ex);
                }
            }).start();
        }
    }
}
