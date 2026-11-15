package com.gizmohub.dto;

/** Body of PATCH /api/admin/products/{id}/stock */
public record StockRequest(Integer stock) {
}
