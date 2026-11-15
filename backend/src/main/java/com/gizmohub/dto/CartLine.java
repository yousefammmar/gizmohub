package com.gizmohub.dto;

import java.math.BigDecimal;

/** One row in the cart view. lineTotal = unitPrice x quantity (CART-06). */
public record CartLine(Long productId, String name, String category, BigDecimal unitPrice,
                       int quantity, BigDecimal lineTotal, int stock) {
}
