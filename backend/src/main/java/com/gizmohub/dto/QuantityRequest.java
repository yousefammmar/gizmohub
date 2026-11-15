package com.gizmohub.dto;

/** Body of PUT /api/cart/items/{productId} */
public record QuantityRequest(Integer quantity) {
}
