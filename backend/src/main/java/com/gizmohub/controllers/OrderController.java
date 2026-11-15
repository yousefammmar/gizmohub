package com.gizmohub.controllers;

import com.gizmohub.dto.CheckoutRequest;
import com.gizmohub.dto.OrderResponse;
import com.gizmohub.dto.OrderTotals;
import com.gizmohub.services.CheckoutService;
import com.gizmohub.services.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Login required (AuthInterceptor -> ORDER-01).
 * GET  /api/checkout/summary   200 {itemCount, subtotal, shippingFee, total}
 * POST /api/orders             201 order | 400 ORDER-02/03/04 | 409 ORDER-05
 * GET  /api/orders             200 own order history
 * GET  /api/orders/{id}        200 own order | 404 ORDER-12
 */
@RestController
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public OrderController(CheckoutService checkoutService, OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @GetMapping("/api/checkout/summary")
    public OrderTotals summary(HttpSession session) {
        return checkoutService.summary(CartController.currentUserId(session));
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody CheckoutRequest request, HttpSession session) {
        OrderResponse order = checkoutService.placeOrder(CartController.currentUserId(session), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/api/orders")
    public List<OrderResponse> history(HttpSession session) {
        return orderService.history(CartController.currentUserId(session));
    }

    @GetMapping("/api/orders/{id}")
    public OrderResponse get(@PathVariable Long id, HttpSession session) {
        return orderService.getOwnOrder(CartController.currentUserId(session), id);
    }
}
