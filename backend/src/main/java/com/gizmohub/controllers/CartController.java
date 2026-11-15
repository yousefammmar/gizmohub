package com.gizmohub.controllers;

import com.gizmohub.dto.CartItemRequest;
import com.gizmohub.dto.CartResponse;
import com.gizmohub.dto.QuantityRequest;
import com.gizmohub.services.AuthService;
import com.gizmohub.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

/**
 * Login required (AuthInterceptor). The user id ALWAYS comes from the session (CART-08).
 * GET    /api/cart                       200 cart
 * POST   /api/cart/items                 200 cart | 400 CART-01/03 | 404 CART-03 | 409 CART-02/04
 * PUT    /api/cart/items/{productId}     200 cart | 400 CART-01 | 404 | 409 CART-02
 * DELETE /api/cart/items/{productId}     200 cart | 404
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(HttpSession session) {
        return cartService.getCart(currentUserId(session));
    }

    @PostMapping("/items")
    public CartResponse add(@RequestBody CartItemRequest request, HttpSession session) {
        return cartService.addItem(currentUserId(session), request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse update(@PathVariable Long productId, @RequestBody QuantityRequest request, HttpSession session) {
        return cartService.updateQuantity(currentUserId(session), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable Long productId, HttpSession session) {
        return cartService.removeItem(currentUserId(session), productId);
    }

    static Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(AuthService.SESSION_USER_ID);
    }
}
