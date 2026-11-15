package com.gizmohub.unit;

import com.gizmohub.validation.CheckoutValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Baseline unit tests for ORDER-03 (address) and ORDER-04 (phone). */
class CheckoutValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"0791234567", "+962791234567", "123456789012345"})
    void validPhonesAreAccepted(String phone) {
        assertThat(CheckoutValidator.validatePhone(phone)).isEqualTo(phone);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"079123456", "1234567890123456", "079-123-4567", "+", "07912+34567", "++962791234567"})
    void invalidPhonesAreRejected(String phone) {
        assertThatThrownBy(() -> CheckoutValidator.validatePhone(phone)).extracting("code").isEqualTo("ORDER-04");
    }

    @Test
    void addressBoundaries() {
        assertThat(CheckoutValidator.validateAddress("  " + "a".repeat(10) + "  ")).hasSize(10);
        CheckoutValidator.validateAddress("a".repeat(200));
        assertThatThrownBy(() -> CheckoutValidator.validateAddress("a".repeat(9))).extracting("code").isEqualTo("ORDER-03");
        assertThatThrownBy(() -> CheckoutValidator.validateAddress("a".repeat(201))).extracting("code").isEqualTo("ORDER-03");
        assertThatThrownBy(() -> CheckoutValidator.validateAddress(null)).extracting("code").isEqualTo("ORDER-03");
    }
}
