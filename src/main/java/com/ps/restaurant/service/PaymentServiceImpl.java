package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.filters.FilterPaymentRequest;
import com.ps.restaurant.interfaces.PaymentService;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.PaymentRepository;
import com.ps.restaurant.repository.specification.PaymentSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<PaymentResponse> filterPayments(FilterPaymentRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        return paymentRepository.findAll(PaymentSpecifications.withFilters(filter)).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    public long countPayments(FilterPaymentRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        return paymentRepository.count(PaymentSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrder(ServiceSupport.requireById(orderRepository, request.orderId(), "Order"));
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.getOrder().getPayments().add(payment);
        return PaymentResponse.from(paymentRepository.save(payment));
    }
}

