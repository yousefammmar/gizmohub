package com.gizmohub.models;

/**
 * Order life cycle. Allowed transitions (see OrderService.changeStatus):
 *   PLACED -> SHIPPED -> DELIVERED
 *   PLACED -> CANCELLED
 */
public enum OrderStatus {
    PLACED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
