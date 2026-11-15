package com.gizmohub.dto;

import java.math.BigDecimal;

/** Body of POST/PUT /api/admin/products. Category is a String so an unknown value gives a clear PROD-04 error. */
public record ProductRequest(String name, String description, String category, BigDecimal price, Integer stock) {
}
