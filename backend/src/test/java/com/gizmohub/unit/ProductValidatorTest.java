package com.gizmohub.unit;

import com.gizmohub.models.Category;
import com.gizmohub.validation.ProductValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Baseline unit tests for PROD-01 .. PROD-04. */
class ProductValidatorTest {

    // PROD-02: 0 < price <= 10000 (min exclusive, max inclusive)
    @ParameterizedTest
    @ValueSource(strings = {"0.01", "0.02", "9999.99", "10000.00", "10000"})
    void pricesInsideRangeAreAccepted(String price) {
        ProductValidator.validatePrice(new BigDecimal(price));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "0", "0.00", "10000.01"})
    void pricesOutsideRangeAreRejected(String price) {
        assertThatThrownBy(() -> ProductValidator.validatePrice(new BigDecimal(price)))
                .extracting("code").isEqualTo("PROD-02");
    }

    @Test
    void priceWithMoreThanTwoDecimalsIsRejected() {
        assertThatThrownBy(() -> ProductValidator.validatePrice(new BigDecimal("1.999")))
                .extracting("code").isEqualTo("PROD-02");
    }

    // PROD-03: 0 <= stock <= 1000
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 999, 1000})
    void stockInsideRangeIsAccepted(int stock) {
        ProductValidator.validateStock(stock);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1001})
    void stockOutsideRangeIsRejected(int stock) {
        assertThatThrownBy(() -> ProductValidator.validateStock(stock)).extracting("code").isEqualTo("PROD-03");
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> ProductValidator.validateName("   ")).extracting("code").isEqualTo("PROD-01");
    }

    @Test
    void categoryIsParsedCaseInsensitively() {
        assertThat(ProductValidator.parseCategory(" audio ")).isEqualTo(Category.AUDIO);
        assertThatThrownBy(() -> ProductValidator.parseCategory("TOYS")).extracting("code").isEqualTo("PROD-04");
        assertThatThrownBy(() -> ProductValidator.parseCategory(null)).extracting("code").isEqualTo("PROD-04");
    }
}
