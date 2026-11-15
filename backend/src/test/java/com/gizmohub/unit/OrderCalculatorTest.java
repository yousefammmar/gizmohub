package com.gizmohub.unit;

import com.gizmohub.dto.OrderTotals;
import com.gizmohub.services.OrderCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Baseline unit tests for CART-06, ORDER-06, ORDER-07, ORDER-08. */
class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    // ORDER-06/07 boundary at 50.00 (inclusive -> free shipping)
    @ParameterizedTest
    @CsvSource({"0.01, 5.00", "49.99, 5.00", "50.00, 0.00", "50.01, 0.00"})
    void shippingFeeAroundFreeShippingThreshold(String subtotal, String expectedFee) {
        assertThat(calculator.shippingFee(new BigDecimal(subtotal))).isEqualByComparingTo(expectedFee);
    }

    @Test
    void totalIsSubtotalPlusShipping() {
        OrderTotals totals = calculator.calculate(List.of(
                TestData.cartItem(1, TestData.product(1, "19.99", 10), 2),
                TestData.cartItem(1, TestData.product(2, "0.01", 10), 1)));
        assertThat(totals.itemCount()).isEqualTo(3);
        assertThat(totals.subtotal()).isEqualByComparingTo("39.99");
        assertThat(totals.shippingFee()).isEqualByComparingTo("5.00");
        assertThat(totals.total()).isEqualByComparingTo("44.99");
    }

    @Test
    void decimalArithmeticIsExact() {
        // 0.10 + 0.20 would be 0.30000000000000004 with double
        OrderTotals totals = calculator.calculate(List.of(
                TestData.cartItem(1, TestData.product(1, "0.10", 10), 1),
                TestData.cartItem(1, TestData.product(2, "0.20", 10), 1)));
        assertThat(totals.subtotal()).isEqualTo(new BigDecimal("0.30"));
    }

    @Test
    void emptyCartHasZeroTotals() {
        OrderTotals totals = calculator.calculate(List.of());
        assertThat(totals.total()).isEqualByComparingTo("0");
        assertThat(totals.shippingFee()).isEqualByComparingTo("0");
    }
}
