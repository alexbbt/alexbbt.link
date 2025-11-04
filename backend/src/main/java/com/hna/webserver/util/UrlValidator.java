package com.hna.webserver.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for validating URLs.
 */
public class UrlValidator {

    private static final String[] ALLOWED_PROTOCOLS = {"http", "https"};

    /**
     * Validates if a URL is well-formed and uses an allowed protocol.
     *
     * @param urlString the URL string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }

        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol().toLowerCase();

            // Check if protocol is allowed
            for (String allowedProtocol : ALLOWED_PROTOCOLS) {
                if (protocol.equals(allowedProtocol)) {
                    return true;
                }
            }
            return false;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Normalizes a URL by adding http:// if no protocol is specified.
     *
     * @param urlString the URL string to normalize
     * @return normalized URL with protocol
     */
    public static String normalize(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return urlString;
        }

        String trimmed = urlString.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        // Default to https for security
        return "https://" + trimmed;
    }

    /**
     * Validates and normalizes a URL.
     *
     * @param urlString the URL string to validate and normalize
     * @return normalized URL if valid, null otherwise
     */
    public static String validateAndNormalize(String urlString) {
        String normalized = normalize(urlString);
        if (isValid(normalized)) {
            return normalized;
        }
        return null;
    }
}
