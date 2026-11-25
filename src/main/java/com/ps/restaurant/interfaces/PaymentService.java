package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.filters.FilterPaymentRequest;
import com.ps.restaurant.model.Payment;

public interface PaymentService {

    Optional<Payment> getPaymentById(Long id);

    List<PaymentResponse> filterPayments(FilterPaymentRequest filter);

    long countPayments(FilterPaymentRequest filter);

    PaymentResponse createPayment(PaymentRequest request);
}

