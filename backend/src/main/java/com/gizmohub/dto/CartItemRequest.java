package com.gizmohub.dto;

/** Body of POST /api/cart/items */
public record CartItemRequest(Long productId, Integer quantity) {
}
