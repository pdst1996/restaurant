package com.ps.restaurant.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ErrorResponseException.NotFoundException;
import com.ps.restaurant.dto.filters.FilterPaymentRequest;
import com.ps.restaurant.interfaces.PaymentService;
import com.ps.restaurant.interfaces.PaymentProvider;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.PaymentStatus;
import com.ps.restaurant.model.MockPaymentOutcome;
import com.ps.restaurant.repository.PaymentRepository;
import com.ps.restaurant.repository.specification.PaymentSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderWorkflow workflow;
    private final OrderAmounts amounts;
    private final List<PaymentProvider> providers;

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<PaymentResponse> filterPayments(FilterPaymentRequest filter) {
        ServiceSupport.validateDateRange(filter.fromDate(), filter.toDate());
        var criteria = PaymentSpecifications.withFilters(filter);
        var payments = filter.page() == null && filter.size() == null ? paymentRepository.findAll(criteria)
                : paymentRepository.findAll(criteria, PageRequest.of(filter.page() == null ? 0 : filter.page(),
                        filter.size() == null ? 50 : filter.size(), Sort.by("id"))).getContent();
        return payments.stream()
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
    public PaymentResponse createPayment(PaymentRequest request, String key) {
        Order order = workflow.lock(request.orderId());
        var actor = workflow.requireActor();
        if (key == null || !key.matches("[a-zA-Z0-9._:-]{1,80}")) {
            throw new ErrorResponseException(422, "INVALID_IDEMPOTENCY_KEY", "A valid Idempotency-Key is required");
        }
        BigDecimal tip = Objects.requireNonNullElse(request.tip(), BigDecimal.ZERO);
        boolean cash = request.method() == PaymentMethod.CASH;
        if ((cash && request.mockOutcome() != null) || (!cash && request.cashReceived() != null)) {
            throw new ErrorResponseException(422, "INVALID_PAYMENT_METHOD_FIELDS", "Payment fields do not match the method");
        }
        MockPaymentOutcome outcome = cash ? null : Objects.requireNonNullElse(request.mockOutcome(), MockPaymentOutcome.APPROVE);
        String fingerprint = fingerprint(request, tip, outcome);
        Optional<Payment> existing = paymentRepository.findByOrderIdAndIdempotencyKey(order.getId(), key);
        if (existing.isPresent()) {
            if (!fingerprint.equals(existing.get().getRequestFingerprint())) {
                throw new ErrorResponseException(409, "PAYMENT_KEY_REUSED", "This payment key was already used with different data");
            }
            return PaymentResponse.from(existing.get());
        }
        if (order.getStatus() != OrderStatus.SERVED) {
            throw new ErrorResponseException(409, "ORDER_NOT_PAYABLE", "Only served orders can receive payments");
        }
        var balance = amounts.summarize(order);
        if (request.amount().compareTo(balance.balance().subtract(balance.pending())) > 0) {
            throw new ErrorResponseException(409, "PAYMENT_EXCEEDS_BALANCE", "The payment exceeds the available balance");
        }
        var result = provider(request.method()).capture(new PaymentProvider.Capture(request.method(),
                request.amount().add(tip), request.cashReceived(), outcome, key));
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setTip(tip);
        payment.setCollectedBy(actor.getId());
        payment.setCashReceived(request.cashReceived());
        payment.setChange(result.change());
        payment.setStatus(result.status());
        payment.setExternalReference(result.reference());
        payment.setIdempotencyKey(key);
        payment.setRequestFingerprint(fingerprint);
        order.getPayments().add(payment);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse refund(Long id, String reason) {
        Long orderId = paymentRepository.findParentId(id).orElseThrow(() -> new NotFoundException("Payment not found"));
        Order order = workflow.lock(orderId);
        var actor = workflow.requireActor();
        Payment payment = ServiceSupport.requireById(paymentRepository, id, "Payment");
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            if (payment.getRefundReason().equals(reason.strip())) return PaymentResponse.from(payment);
            throw new ConflictException("This payment has already been reversed");
        }
        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Only completed or pending payments can be reversed");
        }
        payment.setRefundReference(provider(payment.getMethod()).reverse(payment.getExternalReference(),
                payment.getStatus(), "refund-" + id));
        payment.setRefundReason(reason.strip());
        payment.setRefundedBy(actor.getId());
        payment.setRefundedAt(java.time.Instant.now());
        payment.setStatus(PaymentStatus.REFUNDED);
        workflow.reopenAfterRefund(order, reason);
        return PaymentResponse.from(payment);
    }

    private PaymentProvider provider(PaymentMethod method) {
        return providers.stream().filter(p -> p.supports(method)).findFirst().orElseThrow();
    }

    private String fingerprint(PaymentRequest request, BigDecimal tip, MockPaymentOutcome outcome) {
        String raw = request.method() + "|" + decimal(request.amount()) + "|" + decimal(tip)
                + "|" + decimal(request.cashReceived()) + "|" + outcome;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String decimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}

