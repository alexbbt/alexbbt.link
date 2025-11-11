package com.hna.webserver.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting information from HTTP requests.
 */
public class RequestUtils {

    /**
     * Extracts the client IP address from the request.
     * Handles X-Forwarded-For header for proxied requests.
     * Falls back to RemoteAddr if no proxy headers are present.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (for proxies/load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, the first one is the original client
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }

        // Check X-Real-IP header (alternative proxy header)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        // Fall back to remote address
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }

        return "unknown";
    }

    /**
     * Gets the referrer URL from the request.
     *
     * @param request the HTTP request
     * @return the referrer URL, or null if not present
     */
    public static String getReferrer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = request.getHeader("Referrer"); // Some browsers use this
        }
        return referer != null && !referer.isEmpty() ? referer : null;
    }

    /**
     * Gets the User-Agent string from the request.
     *
     * @param request the HTTP request
     * @return the User-Agent string, or null if not present
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
