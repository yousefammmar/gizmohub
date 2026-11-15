package com.gizmohub.unit;

import com.gizmohub.services.OrderService;
import org.junit.jupiter.api.Test;

import static com.gizmohub.models.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Baseline unit tests for ORDER-11 status transitions. */
class OrderServiceTest {

    @Test
    void allowedTransitions() {
        assertThat(OrderService.isAllowedTransition(PLACED, SHIPPED)).isTrue();
        assertThat(OrderService.isAllowedTransition(PLACED, CANCELLED)).isTrue();
        assertThat(OrderService.isAllowedTransition(SHIPPED, DELIVERED)).isTrue();
    }

    @Test
    void forbiddenTransitions() {
        assertThat(OrderService.isAllowedTransition(SHIPPED, CANCELLED)).isFalse();
        assertThat(OrderService.isAllowedTransition(DELIVERED, PLACED)).isFalse();
        assertThat(OrderService.isAllowedTransition(PLACED, DELIVERED)).isFalse();
    }
}
