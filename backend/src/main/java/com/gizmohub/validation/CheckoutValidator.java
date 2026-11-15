package com.gizmohub.validation;

/** Input rules for Checkout (Team Member 4). Pure functions. */
public final class CheckoutValidator {

    public static final int MIN_ADDRESS_LENGTH = 10;  // ORDER-03, inclusive (after trimming)
    public static final int MAX_ADDRESS_LENGTH = 200; // ORDER-03, inclusive
    public static final int MIN_PHONE_DIGITS = 10;    // ORDER-04, inclusive
    public static final int MAX_PHONE_DIGITS = 15;    // ORDER-04, inclusive

    private CheckoutValidator() {
    }

    /** ORDER-03: required; 10..200 characters after trimming. Returns the trimmed address. */
    public static String validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw BusinessException.badRequest("ORDER-03", "Shipping address is required.");
        }
        String trimmed = address.trim();
        if (trimmed.length() < MIN_ADDRESS_LENGTH || trimmed.length() > MAX_ADDRESS_LENGTH) {
            throw BusinessException.badRequest("ORDER-03",
                    "Shipping address must be between " + MIN_ADDRESS_LENGTH + " and " + MAX_ADDRESS_LENGTH + " characters.");
        }
        return trimmed;
    }

    /**
     * ORDER-04 phone rule (documented in requirements):
     *   - optional single '+' as the FIRST character only
     *   - every other character must be a digit 0-9 (no spaces, dashes or brackets)
     *   - number of digits between 10 and 15 inclusive
     * Examples: valid "0791234567", "+962791234567"; invalid "079-123-4567", "12345", "+".
     *
     * Written as a simple loop (instead of a regex) on purpose: it is easy to draw its
     * control-flow graph and follow its variables for data-flow / LCSAJ analysis.
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw BusinessException.badRequest("ORDER-04", "Phone number is required.");
        }
        String trimmed = phone.trim();
        int digitCount = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (i == 0 && c == '+') {
                continue; // leading plus is allowed
            }
            if (c < '0' || c > '9') {
                throw BusinessException.badRequest("ORDER-04", "Phone number may contain only digits and an optional leading +.");
            }
            digitCount++;
        }
        if (digitCount < MIN_PHONE_DIGITS || digitCount > MAX_PHONE_DIGITS) {
            throw BusinessException.badRequest("ORDER-04",
                    "Phone number must have between " + MIN_PHONE_DIGITS + " and " + MAX_PHONE_DIGITS + " digits.");
        }
        return trimmed;
    }
}
