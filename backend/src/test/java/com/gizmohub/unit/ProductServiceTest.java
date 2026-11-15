package com.gizmohub.unit;

import com.gizmohub.models.Category;
import com.gizmohub.models.Product;
import com.gizmohub.repositories.CartItemRepository;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.services.ProductService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/** Baseline unit tests for search filtering (PROD-06, PROD-07, PROD-08). */
class ProductServiceTest {

    private final ProductService service = new ProductService(mock(ProductRepository.class), mock(CartItemRepository.class));
    private final Product speaker = new Product("BassBox Speaker", "Portable speaker", Category.AUDIO, new BigDecimal("50.00"), 12);

    @Test
    void priceRangeBoundsAreInclusive() {
        BigDecimal fifty = new BigDecimal("50.00");
        assertThat(ProductService.matchesFilters(speaker, null, null, fifty, fifty)).isTrue();
        assertThat(ProductService.matchesFilters(speaker, null, null, new BigDecimal("50.01"), null)).isFalse();
        assertThat(ProductService.matchesFilters(speaker, null, null, null, new BigDecimal("49.99"))).isFalse();
    }

    @Test
    void textMatchesNameOrDescription() {
        assertThat(ProductService.matchesFilters(speaker, "bassbox", null, null, null)).isTrue();
        assertThat(ProductService.matchesFilters(speaker, "portable", null, null, null)).isTrue();
        assertThat(ProductService.matchesFilters(speaker, "laptop", null, null, null)).isFalse();
        assertThat(ProductService.matchesFilters(speaker, null, Category.PHONES, null, null)).isFalse();
    }

    @Test
    void invalidFiltersAreRejectedWithProd08() {
        assertThatThrownBy(() -> service.search(null, null, "abc", null)).extracting("code").isEqualTo("PROD-08");
        assertThatThrownBy(() -> service.search(null, null, "-1", null)).extracting("code").isEqualTo("PROD-08");
        assertThatThrownBy(() -> service.search(null, null, "100", "10")).extracting("code").isEqualTo("PROD-08");
        assertThatThrownBy(() -> service.search(null, "TOYS", null, null)).extracting("code").isEqualTo("PROD-08");
    }

    @Test
    void zeroStockIsLabelledOutOfStock() {
        assertThat(ProductService.stockLabel(0)).isEqualTo("Out of Stock");
        assertThat(ProductService.stockLabel(1)).isEqualTo("In Stock");
    }
}
