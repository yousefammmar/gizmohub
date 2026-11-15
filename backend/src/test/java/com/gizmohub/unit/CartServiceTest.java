package com.gizmohub.unit;

import com.gizmohub.dto.CartResponse;
import com.gizmohub.models.CartItem;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.services.CartService;
import com.gizmohub.services.OrderCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Baseline unit tests for CartService.addItem (CART-01..05) using Mockito instead of a database. */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final long USER = 7L;

    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartItemRepository, productRepository, new OrderCalculator());
    }

    @Test
    void addsNewProductToCart() {
        Product product = TestData.product(1, "10.00", 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(USER, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdOrderByIdAsc(USER)).thenReturn(List.of(TestData.cartItem(USER, product, 2)));

        CartResponse cart = cartService.addItem(USER, 1L, 2);

        verify(cartItemRepository).save(any(CartItem.class));
        assertThat(cart.subtotal()).isEqualByComparingTo("20.00");
    }

    @Test
    void addingExistingProductIncreasesQuantityInsteadOfNewRow() {
        Product product = TestData.product(1, "10.00", 5);
        CartItem existing = TestData.cartItem(USER, product, 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(USER, 1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByUserIdOrderByIdAsc(USER)).thenReturn(List.of(existing));

        cartService.addItem(USER, 1L, 3);

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void rejectsQuantityOutsideOneToTen() {
        assertThatThrownBy(() -> cartService.addItem(USER, 1L, 0)).extracting("code").isEqualTo("CART-01");
        assertThatThrownBy(() -> cartService.addItem(USER, 1L, 11)).extracting("code").isEqualTo("CART-01");
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsNonexistentProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.addItem(USER, 99L, 1)).extracting("code").isEqualTo("CART-03");
    }

    @Test
    void rejectsOutOfStockProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(TestData.product(1, "10.00", 0)));
        assertThatThrownBy(() -> cartService.addItem(USER, 1L, 1)).extracting("code").isEqualTo("CART-04");
    }

    @Test
    void rejectsQuantityAboveStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(TestData.product(1, "10.00", 3)));
        when(cartItemRepository.findByUserIdAndProductId(USER, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.addItem(USER, 1L, 4)).extracting("code").isEqualTo("CART-02");
    }
}
