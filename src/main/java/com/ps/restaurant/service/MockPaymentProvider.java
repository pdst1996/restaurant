package com.ps.restaurant.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ps.restaurant.interfaces.PaymentProvider;
import com.ps.restaurant.model.MockPaymentOutcome;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.PaymentStatus;

@Component
public class MockPaymentProvider implements PaymentProvider {
    @Override
    public boolean supports(PaymentMethod method) { return method != PaymentMethod.CASH; }

    @Override
    public CaptureResult capture(Capture input) {
        var outcome = input.outcome() == null ? MockPaymentOutcome.APPROVE : input.outcome();
        var status = switch (outcome) {
            case APPROVE -> PaymentStatus.COMPLETED;
            case DECLINE -> PaymentStatus.FAILED;
            case PENDING -> PaymentStatus.PENDING;
        };
        return new CaptureResult(status, "mock-" + UUID.randomUUID(), null);
    }

    @Override
    public String reverse(String reference, PaymentStatus status, String idempotencyKey) {
        return "mock-reversal-" + UUID.randomUUID();
    }
}
