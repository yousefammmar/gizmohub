package com.gizmohub.dto;

import java.math.BigDecimal;

/** Product as sent to the browser. stockLabel implements PROD-06 ("Out of Stock" when stock is 0). */
public record ProductResponse(Long id, String name, String description, String category,
                              BigDecimal price, int stock, boolean inStock, String stockLabel) {
}
