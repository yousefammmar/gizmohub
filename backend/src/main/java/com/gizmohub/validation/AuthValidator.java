package com.gizmohub.validation;

import java.util.regex.Pattern;

/**
 * Input rules for the Authentication module (Team Member 1).
 * Pure functions: no database, so they are easy to test with EQP and BVA.
 */
public final class AuthValidator {

    public static final int MIN_PASSWORD_LENGTH = 8;   // AUTH-03, inclusive
    public static final int MAX_PASSWORD_LENGTH = 20;  // AUTH-03, inclusive
    public static final int MAX_EMAIL_LENGTH = 254;    // AUTH-01, inclusive

    /**
     * AUTH-01 email format: local-part "@" domain "." top-level-domain
     *   local-part : letters, digits and . _ % + -   (at least 1 character)
     *   domain     : letters, digits, . and -        (at least 1 character)
     *   TLD        : letters only, at least 2
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private AuthValidator() {
    }

    /** AUTH-01. Returns the email trimmed and lower-cased, or throws. */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw BusinessException.badRequest("AUTH-01", "Email is required.");
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw BusinessException.badRequest("AUTH-01", "Please enter a valid email address.");
        }
        return normalized;
    }

    /** AUTH-03: 8 <= length <= 20. */
    public static void validatePassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {
            throw BusinessException.badRequest("AUTH-03",
                    "Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters.");
        }
    }

    /** AUTH-04: confirmation must be exactly equal (case-sensitive). */
    public static void validatePasswordConfirmation(String password, String confirmPassword) {
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            throw BusinessException.badRequest("AUTH-04", "Password confirmation does not match.");
        }
    }
}
