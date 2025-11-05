package com.hna.webserver.controller;

import com.hna.webserver.service.ShortLinkService;
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

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    /**
     * Handles root-level redirects for short links.
     * This catches all routes that aren't reserved paths or static files.
     * Uses regex to exclude paths containing dots (file extensions).
     * Note: This mapping excludes reserved paths (admin, api, _next) and files
     * which are handled by static resources or other controllers.
     *
     * @param slug the slug to redirect
     * @param response HTTP response to set redirect
     * @throws IOException if redirect fails
     */
    @GetMapping("/{slug:[^.]+}")
    public void redirect(@PathVariable String slug, HttpServletResponse response) throws IOException {
        // Skip reserved paths - these should never reach here due to regex pattern and static resources
        // But handle gracefully just in case
        if (RESERVED_PATHS.contains(slug.toLowerCase())) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        try {
            // Get original URL (with caching)
            String originalUrl = shortLinkService.getOriginalUrl(slug);

            // Increment click count asynchronously (fire and forget)
            // Using a separate thread to avoid blocking the redirect
            new Thread(() -> {
                try {
                    shortLinkService.incrementClickCount(slug);
                } catch (Exception e) {
                    logger.error("Failed to increment click count for slug: {}", slug, e);
                }
            }).start();

            // Perform redirect
            response.sendRedirect(originalUrl);
            logger.debug("Redirected slug {} to {}", slug, originalUrl);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid slug or link not found: {}", slug);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("Short link not found");
        } catch (Exception e) {
            logger.error("Error redirecting slug: {}", slug, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write("Internal server error");
        }
    }
}
