package com.gizmohub.services;

import com.gizmohub.dto.OrderTotals;
import com.gizmohub.models.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * All money arithmetic in one place (CART-06, ORDER-06, ORDER-07, ORDER-08).
 * Uses BigDecimal with 2 decimals: 0.1 + 0.2 is exactly 0.30 here (it would not be with double).
 */
@Component
public class OrderCalculator {

    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00"); // ORDER-06: subtotal >= 50 -> free
    public static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("5.00");    // ORDER-07: subtotal < 50 -> 5
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    /** CART-06: one line = unit price x quantity. */
    public BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    /** CART-06: subtotal = sum of all line totals. */
    public BigDecimal subtotal(List<CartItem> items) {
        BigDecimal subtotal = ZERO;
        for (CartItem item : items) {
            subtotal = subtotal.add(lineTotal(item.getProduct().getPrice(), item.getQuantity()));
        }
        return subtotal;
    }

    /** ORDER-06 / ORDER-07. An empty cart (subtotal 0) is rejected earlier by ORDER-02. */
    public BigDecimal shippingFee(BigDecimal subtotal) {
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return ZERO;
        }
        return STANDARD_SHIPPING_FEE;
    }

    /** ORDER-08: total = subtotal + shipping fee. */
    public OrderTotals calculate(List<CartItem> items) {
        int itemCount = 0;
        for (CartItem item : items) {
            itemCount += item.getQuantity();
        }
        BigDecimal subtotal = subtotal(items);
        BigDecimal shipping = items.isEmpty() ? ZERO : shippingFee(subtotal);
        BigDecimal total = subtotal.add(shipping);
        return new OrderTotals(itemCount, subtotal, shipping, total);
    }
}
