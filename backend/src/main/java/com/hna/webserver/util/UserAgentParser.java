package com.hna.webserver.util;

/**
 * Simple utility to parse basic information from User-Agent strings.
 * For production, consider using a library like ua-parser-java for more accurate parsing.
 */
public class UserAgentParser {

    /**
     * Parses device type from User-Agent string.
     *
     * @param userAgent the User-Agent string
     * @return device type (mobile, tablet, desktop, bot) or null if unknown
     */
    public static String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        String ua = userAgent.toLowerCase();

        // Check for bots/crawlers
        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider") ||
            ua.contains("scraper") || ua.contains("curl") || ua.contains("wget")) {
            return "bot";
        }

        // Check for mobile devices
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") ||
            ua.contains("ipod") || ua.contains("blackberry") || ua.contains("windows phone")) {
            return "mobile";
        }

        // Check for tablets
        if (ua.contains("tablet") || ua.contains("ipad") || (ua.contains("android") && !ua.contains("mobile"))) {
            return "tablet";
        }

        // Default to desktop
        return "desktop";
    }

    /**
     * Parses browser name from User-Agent string.
     *
     * @param userAgent the User-Agent string
     * @return browser name or null if unknown
     */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("edg")) {
            return "Edge";
        }
        if (ua.contains("chrome") && !ua.contains("edg")) {
            return "Chrome";
        }
        if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        }
        if (ua.contains("firefox")) {
            return "Firefox";
        }
        if (ua.contains("opera") || ua.contains("opr")) {
            return "Opera";
        }
        if (ua.contains("msie") || ua.contains("trident")) {
            return "Internet Explorer";
        }

        return "Unknown";
    }

    /**
     * Parses operating system from User-Agent string.
     *
     * @param userAgent the User-Agent string
     * @return operating system name or null if unknown
     */
    public static String parseOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("windows")) {
            if (ua.contains("windows nt 10.0") || ua.contains("windows 10")) {
                return "Windows 10";
            }
            if (ua.contains("windows nt 6.3") || ua.contains("windows 8.1")) {
                return "Windows 8.1";
            }
            if (ua.contains("windows nt 6.2") || ua.contains("windows 8")) {
                return "Windows 8";
            }
            if (ua.contains("windows nt 6.1") || ua.contains("windows 7")) {
                return "Windows 7";
            }
            return "Windows";
        }
        if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return "iOS";
        }

        return "Unknown";
    }
}
