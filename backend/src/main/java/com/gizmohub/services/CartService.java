package com.gizmohub.services;

import com.gizmohub.dto.CartLine;
import com.gizmohub.dto.CartResponse;
import com.gizmohub.models.CartItem;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.validation.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shopping Cart module (Team Member 3): rules CART-01 .. CART-08.
 * Every method takes the userId from the session (never from the request body) -> CART-08.
 */
@Service
public class CartService {

    public static final int MIN_QUANTITY = 1;  // CART-01, inclusive
    public static final int MAX_QUANTITY = 10; // CART-01, inclusive (per product, total in cart)

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderCalculator calculator;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository,
                       OrderCalculator calculator) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return buildResponse(cartItemRepository.findByUserIdOrderByIdAsc(userId));
    }

    /**
     * Adds a product. If it is already in the cart the quantities are added together (CART-05).
     * Order of checks: quantity range -> product exists -> in stock -> combined quantity -> stock.
     */
    @Transactional
    public CartResponse addItem(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);                                                  // CART-01
        Product product = findProduct(productId);                                    // CART-03
        if (product.getStock() == 0) {                                               // CART-04
            throw BusinessException.conflict("CART-04", product.getName() + " is out of stock.");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        int newQuantity = quantity;
        if (existing.isPresent()) {                                                  // CART-05
            newQuantity = existing.get().getQuantity() + quantity;
        }
        if (newQuantity > MAX_QUANTITY) {                                            // CART-01
            throw BusinessException.badRequest("CART-01",
                    "You can have at most " + MAX_QUANTITY + " of one product in your cart.");
        }
        if (newQuantity > product.getStock()) {                                      // CART-02
            throw BusinessException.conflict("CART-02",
                    "Only " + product.getStock() + " of " + product.getName() + " available.");
        }

        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
        } else {
            cartItemRepository.save(new CartItem(userId, product, newQuantity));
        }
        return getCart(userId);
    }

    /** Sets the quantity of a product that is already in the cart (replaces, does not add). */
    @Transactional
    public CartResponse updateQuantity(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);                                                  // CART-01
        CartItem item = findOwnItem(userId, productId);
        if (quantity > item.getProduct().getStock()) {                               // CART-02
            throw BusinessException.conflict("CART-02",
                    "Only " + item.getProduct().getStock() + " of " + item.getProduct().getName() + " available.");
        }
        item.setQuantity(quantity);
        return getCart(userId);
    }

    /** CART-07: removing an item returns the recalculated cart. */
    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        CartItem item = findOwnItem(userId, productId);
        cartItemRepository.delete(item);
        return getCart(userId);
    }

    // ---------- helpers ----------

    static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw BusinessException.badRequest("CART-01",
                    "Quantity must be between " + MIN_QUANTITY + " and " + MAX_QUANTITY + ".");
        }
    }

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw BusinessException.badRequest("CART-03", "productId is required.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("CART-03", "Product " + productId + " does not exist."));
    }

    /** CART-08: looks the row up by (userId, productId), so another customer's cart row is simply "not found". */
    private CartItem findOwnItem(Long userId, Long productId) {
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> BusinessException.notFound("CART-NOT-IN-CART", "This product is not in your cart."));
    }

    private CartResponse buildResponse(List<CartItem> items) {
        List<CartLine> lines = new ArrayList<>();
        for (CartItem item : items) {
            Product p = item.getProduct();
            lines.add(new CartLine(p.getId(), p.getName(), p.getCategory().name(), p.getPrice(),
                    item.getQuantity(), calculator.lineTotal(p.getPrice(), item.getQuantity()), p.getStock()));
        }
        return new CartResponse(lines, calculator.calculate(items).itemCount(), calculator.subtotal(items));
    }
}
