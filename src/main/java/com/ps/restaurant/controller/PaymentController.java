package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.OperationReasonRequest;
import com.ps.restaurant.dto.filters.FilterPaymentRequest;
import com.ps.restaurant.interfaces.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @Valid @ModelAttribute FilterPaymentRequest filter) {
        return ResponseEntity.ok(paymentService.filterPayments(filter));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        return ResponseEntity.ok(paymentService.createPayment(request, key));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable Long id, @Valid @RequestBody OperationReasonRequest request) {
        return paymentService.refund(id, request.reason());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPaymentsCount(@Valid @ModelAttribute FilterPaymentRequest filter) {
        return ResponseEntity.ok(paymentService.countPayments(filter));
    }
}

