package com.gizmohub.dto;

/** Body of POST /api/orders. Payment is simulated: no card data is ever sent. */
public record CheckoutRequest(String shippingAddress, String phone) {
}
