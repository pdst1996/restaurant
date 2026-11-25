package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.PaymentStatus;

/** Exposes payment details and its order identifier without a parent entity reference. */
public record PaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentMethod method, Instant createdAt,
        PaymentStatus status, BigDecimal tip, BigDecimal cashReceived, BigDecimal change, String externalReference,
        Long collectedBy, Long refundedBy, Instant refundedAt, String refundReason, String refundReference,
        PaymentStatus reversedFrom) {
    public PaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentMethod method, Instant createdAt) {
        this(id, orderId, amount, method, createdAt, PaymentStatus.COMPLETED, BigDecimal.ZERO,
                null, null, null, null, null, null, null, null, null);
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getCreatedAt(), payment.getStatus(), payment.getTip(), payment.getCashReceived(),
                payment.getChange(), payment.getExternalReference(), payment.getCollectedBy(),
                payment.getRefundedBy(), payment.getRefundedAt(), payment.getRefundReason(), payment.getRefundReference(),
                payment.getReversedFrom());
    }
}
