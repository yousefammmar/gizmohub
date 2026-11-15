package com.gizmohub.dto;

/** Body of PATCH /api/admin/orders/{id}/status */
public record StatusRequest(String status) {
}
