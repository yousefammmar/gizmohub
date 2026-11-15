package com.gizmohub.validation;

import com.gizmohub.models.Category;
import java.math.BigDecimal;

/** Input rules for Product Management (Team Member 2). Pure functions. */
public final class ProductValidator {

    public static final int MAX_NAME_LENGTH = 100;                        // PROD-01
    public static final BigDecimal MIN_PRICE_EXCLUSIVE = BigDecimal.ZERO;  // PROD-02: price > 0
    public static final BigDecimal MAX_PRICE = new BigDecimal("10000.00"); // PROD-02: price <= 10,000
    public static final int MIN_STOCK = 0;                                 // PROD-03, inclusive
    public static final int MAX_STOCK = 1000;                              // PROD-03, inclusive

    private ProductValidator() {
    }

    /** PROD-01: not null, not blank, at most 100 characters after trimming. Returns trimmed name. */
    public static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw BusinessException.badRequest("PROD-01", "Product name cannot be empty.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw BusinessException.badRequest("PROD-01", "Product name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    /** PROD-02: 0 < price <= 10,000 with at most 2 decimal places (cents). */
    public static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw BusinessException.badRequest("PROD-02", "Price is required.");
        }
        boolean aboveMinimum = price.compareTo(MIN_PRICE_EXCLUSIVE) > 0;
        boolean withinMaximum = price.compareTo(MAX_PRICE) <= 0;
        if (!aboveMinimum || !withinMaximum) {
            throw BusinessException.badRequest("PROD-02", "Price must be greater than 0 and at most 10,000.");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw BusinessException.badRequest("PROD-02", "Price can have at most 2 decimal places.");
        }
    }

    /** PROD-03: 0 <= stock <= 1000. */
    public static void validateStock(Integer stock) {
        if (stock == null || stock < MIN_STOCK || stock > MAX_STOCK) {
            throw BusinessException.badRequest("PROD-03", "Stock must be between " + MIN_STOCK + " and " + MAX_STOCK + ".");
        }
    }

    /** PROD-04: category is required and must be one of the Category values (case-insensitive). */
    public static Category parseCategory(String category) {
        if (category == null || category.isBlank()) {
            throw BusinessException.badRequest("PROD-04", "Product category is required.");
        }
        try {
            return Category.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException unknown) {
            throw BusinessException.badRequest("PROD-04", "Unknown category: " + category.trim());
        }
    }
}
