package services;

import models.*;
import repository.PaymentRepository;
import repository.RefundRepository;

import java.util.List;
import java.util.UUID;

public class RefundService {

    private PaymentRepository paymentRepository;
    private RefundRepository refundRepository;

    public RefundService(PaymentRepository paymentRepository,
                         RefundRepository refundRepository) {

        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    public Refund processRefund(String paymentId, String reason) {

        Payment payment = paymentRepository.findById(paymentId);

        if (payment == null) {
            throw new IllegalArgumentException("Payment not found.");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException(
                "Only successful payments can be refunded."
            );
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Refund reason is required.");
        }

        double refundAmount = payment.getFinalAmount();

        String refundId = "REF-" +
            UUID.randomUUID().toString().substring(0, 8);

        Refund refund = new Refund(
            refundId,
            paymentId,
            refundAmount,
            reason
        );

        refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);

        return refund;
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    public List<Refund> getPaymentRefunds(String paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }
}
