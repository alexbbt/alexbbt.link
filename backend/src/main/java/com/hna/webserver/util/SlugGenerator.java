package com.hna.webserver.util;

import java.security.SecureRandom;

/**
 * Utility class for generating random slugs for short links.
 * Uses base62 encoding (0-9, a-z, A-Z) for URL-safe slugs.
 */
public class SlugGenerator {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int DEFAULT_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random slug of the default length (6 characters).
     *
     * @return a random slug string
     */
    public static String generateRandomSlug() {
        return generateRandomSlug(DEFAULT_LENGTH);
    }

    /**
     * Generates a random slug of the specified length.
     *
     * @param length the desired length of the slug
     * @return a random slug string
     */
    public static String generateRandomSlug(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder slug = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            slug.append(BASE62_CHARS.charAt(random.nextInt(BASE62_CHARS.length())));
        }
        return slug.toString();
    }

    /**
     * Validates a slug format.
     * Slugs must be 1-50 characters, alphanumeric with hyphens and underscores.
     *
     * @param slug the slug to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isEmpty() || slug.length() > 50) {
            return false;
        }
        // Allow alphanumeric, hyphens, and underscores
        return slug.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Checks if a slug is in the reserved list.
     *
     * @param slug the slug to check
     * @return true if reserved, false otherwise
     */
    public static boolean isReservedSlug(String slug) {
        if (slug == null) {
            return false;
        }
        String lowerSlug = slug.toLowerCase();
        return lowerSlug.equals("admin") ||
               lowerSlug.equals("api") ||
               lowerSlug.startsWith("_next") ||
               lowerSlug.equals("favicon.ico") ||
               lowerSlug.startsWith("_");
    }
}
