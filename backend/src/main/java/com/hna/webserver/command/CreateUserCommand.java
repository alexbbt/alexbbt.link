package com.hna.webserver.command;

import com.hna.webserver.model.User;
import com.hna.webserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Console command to create users.
 * Usage: java -jar app.jar --create-user
 * Or set environment variable: CREATE_USER=true
 */
@Component
@Order(1)
public class CreateUserCommand implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreateUserCommand.class);

    private final UserService userService;

    public CreateUserCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Check if --create-user flag is present or CREATE_USER env var is set
        boolean shouldCreateUser = false;
        for (String arg : args) {
            if (arg.equals("--create-user") || arg.equals("--createUser")) {
                shouldCreateUser = true;
                break;
            }
        }
        if (!shouldCreateUser && !Boolean.parseBoolean(System.getenv("CREATE_USER"))) {
            return;
        }

        logger.info("=== Create User Command ===");
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter roles (comma-separated, e.g., USER,ADMIN or leave empty for USER): ");
            String rolesInput = scanner.nextLine().trim();
            Set<User.Role> roles = parseRoles(rolesInput);

            User user = userService.createUser(username, password, email, roles);
            logger.info("✅ User created successfully: {} (ID: {})", user.getUsername(), user.getId());
            System.out.println("✅ User created successfully: " + user.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("❌ Failed to create user: {}", e.getMessage());
            System.err.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error creating user", e);
            System.err.println("❌ Unexpected error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private Set<User.Role> parseRoles(String rolesInput) {
        Set<User.Role> roles = new HashSet<>();
        if (rolesInput == null || rolesInput.trim().isEmpty()) {
            roles.add(User.Role.USER);
            return roles;
        }

        String[] roleNames = rolesInput.split(",");
        for (String roleName : roleNames) {
            try {
                roles.add(User.Role.valueOf(roleName.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role: {}, skipping", roleName);
            }
        }

        if (roles.isEmpty()) {
            roles.add(User.Role.USER);
        }
        return roles;
    }
}
