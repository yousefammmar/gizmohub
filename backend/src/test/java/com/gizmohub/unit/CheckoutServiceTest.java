package com.gizmohub.unit;

import com.gizmohub.dto.CheckoutRequest;
import com.gizmohub.dto.OrderResponse;
import com.gizmohub.models.CartItem;
import com.gizmohub.models.Order;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.OrderRepository;
import com.gizmohub.services.CheckoutService;
import com.gizmohub.services.OrderCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Baseline unit tests for CheckoutService.placeOrder (ORDER-02, ORDER-05, ORDER-09). */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final long USER = 7L;
    private static final CheckoutRequest VALID = new CheckoutRequest("12 Rainbow Street, Amman", "0791234567");

    @Mock CartItemRepository cartItemRepository;
    @Mock OrderRepository orderRepository;
    CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(cartItemRepository, orderRepository, new OrderCalculator());
    }

    @Test
    void emptyCartCannotBeCheckedOut() {
        when(cartItemRepository.findByUserIdOrderByIdAsc(USER)).thenReturn(List.of());
        assertThatThrownBy(() -> checkoutService.placeOrder(USER, VALID)).extracting("code").isEqualTo("ORDER-02");
    }

    @Test
    void insufficientStockChangesNothing() {
        Product enough = TestData.product(1, "10.00", 5);
        Product tooFew = TestData.product(2, "10.00", 1);
        when(cartItemRepository.findByUserIdOrderByIdAsc(USER)).thenReturn(List.of(
                TestData.cartItem(USER, enough, 2), TestData.cartItem(USER, tooFew, 2)));

        assertThatThrownBy(() -> checkoutService.placeOrder(USER, VALID)).extracting("code").isEqualTo("ORDER-05");

        assertThat(enough.getStock()).isEqualTo(5);
        assertThat(tooFew.getStock()).isEqualTo(1);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void successfulCheckoutReducesStockAndCreatesOrder() {
        Product product = TestData.product(1, "25.00", 5);
        List<CartItem> cart = List.of(TestData.cartItem(USER, product, 2));
        when(cartItemRepository.findByUserIdOrderByIdAsc(USER)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));

        OrderResponse order = checkoutService.placeOrder(USER, VALID);

        assertThat(product.getStock()).isEqualTo(3);
        assertThat(order.total()).isEqualByComparingTo("50.00"); // 50.00 subtotal -> free shipping
        assertThat(order.shippingFee()).isEqualByComparingTo("0.00");
        verify(cartItemRepository).deleteAll(cart);
    }
}
