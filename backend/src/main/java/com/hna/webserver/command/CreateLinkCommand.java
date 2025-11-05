package com.hna.webserver.command;

import com.hna.webserver.model.ShortLink;
import com.hna.webserver.repository.ShortLinkRepository;
import com.hna.webserver.util.SlugGenerator;
import com.hna.webserver.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Console command to create short links.
 * Usage: java -jar app.jar --create-link
 * Or set environment variable: CREATE_LINK=true
 */
@Component
@Order(2)
public class CreateLinkCommand implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreateLinkCommand.class);

    private final ShortLinkRepository shortLinkRepository;

    public CreateLinkCommand(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    @Override
    public void run(String... args) {
        // Check if --create-link flag is present or CREATE_LINK env var is set
        boolean shouldCreateLink = false;
        for (String arg : args) {
            if (arg.equals("--create-link") || arg.equals("--createLink")) {
                shouldCreateLink = true;
                break;
            }
        }
        if (!shouldCreateLink && !Boolean.parseBoolean(System.getenv("CREATE_LINK"))) {
            return;
        }

        logger.info("=== Create Short Link Command ===");
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter URL to shorten: ");
            String url = scanner.nextLine().trim();

            // Validate and normalize URL
            String normalizedUrl = UrlValidator.validateAndNormalize(url);

            System.out.print("Enter custom slug (or leave empty for random): ");
            String slugInput = scanner.nextLine().trim();

            String slug;
            if (slugInput.isEmpty()) {
                // Generate random slug
                slug = SlugGenerator.generateRandomSlug();
                int attempts = 0;
                while (shortLinkRepository.existsBySlugIgnoreCase(slug) && attempts < 10) {
                    slug = SlugGenerator.generateRandomSlug();
                    attempts++;
                }
                if (shortLinkRepository.existsBySlugIgnoreCase(slug)) {
                    throw new IllegalArgumentException("Failed to generate unique slug after 10 attempts");
                }
            } else {
                // Validate custom slug
                if (!SlugGenerator.isValidSlug(slugInput)) {
                    throw new IllegalArgumentException("Invalid slug format. Use alphanumeric characters, hyphens, or underscores.");
                }
                if (SlugGenerator.isReservedSlug(slugInput)) {
                    throw new IllegalArgumentException("Slug is reserved: " + slugInput);
                }
                if (shortLinkRepository.existsBySlugIgnoreCase(slugInput)) {
                    throw new IllegalArgumentException("Slug already exists: " + slugInput);
                }
                slug = slugInput;
            }

            System.out.print("Enter expiration days (or leave empty for no expiration): ");
            String expiryInput = scanner.nextLine().trim();
            LocalDateTime expiresAt = null;
            if (!expiryInput.isEmpty()) {
                try {
                    int days = Integer.parseInt(expiryInput);
                    expiresAt = LocalDateTime.now().plusDays(days);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid expiration days: {}, ignoring", expiryInput);
                }
            }

            // Create short link
            ShortLink shortLink = new ShortLink();
            shortLink.setSlug(slug);
            shortLink.setOriginalUrl(normalizedUrl);
            shortLink.setExpiresAt(expiresAt);
            shortLink.setIsActive(true);

            ShortLink saved = shortLinkRepository.save(shortLink);
            logger.info("✅ Short link created successfully: {} -> {} (ID: {})",
                    saved.getSlug(), saved.getOriginalUrl(), saved.getId());
            System.out.println("✅ Short link created successfully!");
            System.out.println("   Slug: " + saved.getSlug());
            System.out.println("   URL: " + saved.getOriginalUrl());
            if (saved.getExpiresAt() != null) {
                System.out.println("   Expires: " + saved.getExpiresAt());
            }

        } catch (IllegalArgumentException e) {
            logger.error("❌ Failed to create short link: {}", e.getMessage());
            System.err.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error creating short link", e);
            System.err.println("❌ Unexpected error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
