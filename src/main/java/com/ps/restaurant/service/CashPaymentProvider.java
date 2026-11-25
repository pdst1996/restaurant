package com.ps.restaurant.service;

import org.springframework.stereotype.Component;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.interfaces.PaymentProvider;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.PaymentStatus;

@Component
public class CashPaymentProvider implements PaymentProvider {
    @Override
    public boolean supports(PaymentMethod method) { return method == PaymentMethod.CASH; }

    @Override
    public CaptureResult capture(Capture input) {
        if (input.cashReceived() == null || input.cashReceived().compareTo(input.total()) < 0) {
            throw new ErrorResponseException(422, "INSUFFICIENT_CASH", "Cash received must cover the amount plus tip");
        }
        return new CaptureResult(PaymentStatus.COMPLETED, null, input.cashReceived().subtract(input.total()));
    }

    @Override
    public String reverse(String reference, PaymentStatus status, String idempotencyKey) { return null; }
}
