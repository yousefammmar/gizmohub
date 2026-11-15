package com.gizmohub.dto;

import java.math.BigDecimal;
import java.util.List;

/** GET /api/cart. subtotal = sum of all lineTotal values (CART-06). */
public record CartResponse(List<CartLine> items, int itemCount, BigDecimal subtotal) {
}
