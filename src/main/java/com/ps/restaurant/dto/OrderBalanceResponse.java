package com.ps.restaurant.dto;

import java.math.BigDecimal;

public record OrderBalanceResponse(Long orderId, String currency, BigDecimal subtotal, BigDecimal tax,
        BigDecimal total, BigDecimal paid, BigDecimal pending, BigDecimal balance, BigDecimal tip) {}
