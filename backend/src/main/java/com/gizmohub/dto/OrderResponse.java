package com.gizmohub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, Long userId, LocalDateTime createdAt, String status, String shippingAddress,
                            String phone, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total,
                            List<OrderLine> items) {
}
