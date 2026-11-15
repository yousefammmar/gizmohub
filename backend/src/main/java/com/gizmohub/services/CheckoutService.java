package com.gizmohub.services;

import com.gizmohub.dto.CheckoutRequest;
import com.gizmohub.dto.OrderResponse;
import com.gizmohub.dto.OrderTotals;
import com.gizmohub.models.CartItem;
import com.gizmohub.models.Order;
import com.gizmohub.models.OrderItem;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.OrderRepository;
import com.gizmohub.validation.BusinessException;
import com.gizmohub.validation.CheckoutValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Checkout & Orders module (Team Member 4): rules ORDER-01 .. ORDER-10. Payment is simulated. */
@Service
public class CheckoutService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderCalculator calculator;

    public CheckoutService(CartItemRepository cartItemRepository, OrderRepository orderRepository,
                           OrderCalculator calculator) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.calculator = calculator;
    }

    /** Order summary shown on the checkout page before placing the order. */
    @Transactional(readOnly = true)
    public OrderTotals summary(Long userId) {
        return calculator.calculate(cartItemRepository.findByUserIdOrderByIdAsc(userId));
    }

    /**
     * Places an order from the user's cart.
     *
     * ORDER-10: the whole method is ONE database transaction. All checks run before any stock
     * is changed, and if anything throws, every change (stock, order, cart) is rolled back.
     */
    @Transactional
    public OrderResponse placeOrder(Long userId, CheckoutRequest request) {
        if (userId == null) {                                                              // ORDER-01
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "ORDER-01", "Please log in to check out.");
        }
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdAsc(userId);
        if (cartItems.isEmpty()) {                                                         // ORDER-02
            throw BusinessException.badRequest("ORDER-02", "Your cart is empty.");
        }
        String address = CheckoutValidator.validateAddress(request.shippingAddress());     // ORDER-03
        String phone = CheckoutValidator.validatePhone(request.phone());                   // ORDER-04

        // ORDER-05: first collect every problem, without changing anything.
        List<String> unavailable = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStock()) {
                unavailable.add(product.getName() + " (requested " + item.getQuantity()
                        + ", available " + product.getStock() + ")");
            }
        }
        if (!unavailable.isEmpty()) {
            throw BusinessException.conflict("ORDER-05", "Not enough stock for: " + String.join(", ", unavailable));
        }

        OrderTotals totals = calculator.calculate(cartItems);                              // ORDER-06/07/08
        Order order = new Order(userId, address, phone, totals.subtotal(), totals.shippingFee(), totals.total());

        // ORDER-09: reduce inventory and copy each line into the order.
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(),
                    item.getQuantity(), calculator.lineTotal(product.getPrice(), item.getQuantity())));
        }
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        return OrderService.toResponse(saved);
    }
}
