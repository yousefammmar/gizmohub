package com.gizmohub.unit;

import com.gizmohub.validation.AuthValidator;
import com.gizmohub.validation.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Baseline unit tests for AUTH-01, AUTH-03, AUTH-04 (EQP + BVA). Teams extend these in Phase 1/2. */
class AuthValidatorTest {

    @Test
    void validEmailIsTrimmedAndLowerCased() {
        assertThat(AuthValidator.normalizeEmail("  Sara.K@Example.COM ")).isEqualTo("sara.k@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"plainaddress", "no-at.example.com", "a@b", "a@b.c", "@example.com", "a b@example.com"})
    void invalidEmailsAreRejectedWithAuth01(String email) {
        assertThatThrownBy(() -> AuthValidator.normalizeEmail(email))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH-01");
    }

    // AUTH-03 boundaries: 7 (below min), 8 (min), 20 (max), 21 (above max)
    @ParameterizedTest
    @ValueSource(ints = {8, 9, 19, 20})
    void passwordLengthsInsideRangeAreAccepted(int length) {
        AuthValidator.validatePassword("a".repeat(length));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, 21})
    void passwordLengthsOutsideRangeAreRejected(int length) {
        assertThatThrownBy(() -> AuthValidator.validatePassword("a".repeat(length)))
                .extracting("code").isEqualTo("AUTH-03");
    }

    @Test
    void nullPasswordIsRejected() {
        assertThatThrownBy(() -> AuthValidator.validatePassword(null)).extracting("code").isEqualTo("AUTH-03");
    }

    @Test
    void confirmationMustMatchExactly() {
        AuthValidator.validatePasswordConfirmation("Secret123", "Secret123");
        assertThatThrownBy(() -> AuthValidator.validatePasswordConfirmation("Secret123", "secret123"))
                .extracting("code").isEqualTo("AUTH-04");
        assertThatThrownBy(() -> AuthValidator.validatePasswordConfirmation("Secret123", null))
                .extracting("code").isEqualTo("AUTH-04");
    }
}
