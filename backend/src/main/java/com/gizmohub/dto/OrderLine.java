package com.gizmohub.dto;

import java.math.BigDecimal;

public record OrderLine(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
