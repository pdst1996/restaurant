package com.ps.restaurant.interfaces;

import java.math.BigDecimal;
import com.ps.restaurant.model.MockPaymentOutcome;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.PaymentStatus;

/** Provider boundary. Real network integrations need their own asynchronous reconciliation workflow. */
public interface PaymentProvider {
    boolean supports(PaymentMethod method);

    CaptureResult capture(Capture request);

    String reverse(String reference, PaymentStatus status, String idempotencyKey);

    record Capture(PaymentMethod method, BigDecimal total, BigDecimal cashReceived, MockPaymentOutcome outcome,
            String idempotencyKey) {}

    record CaptureResult(PaymentStatus status, String reference, BigDecimal change) {}
}
