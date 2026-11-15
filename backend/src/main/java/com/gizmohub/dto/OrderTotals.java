package com.gizmohub.dto;

import java.math.BigDecimal;

/** Result of OrderCalculator: used for the order summary and for the placed order. */
public record OrderTotals(int itemCount, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total) {
}
