package services;

import models.*;
import repository.PaymentRepository;

import java.util.List;

public class PaymentService {

    private PaymentRepository paymentRepository;
    private PricingService pricingService;

    public PaymentService(PaymentRepository paymentRepository,
                          PricingService pricingService) {

        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
    }

    public Payment processPayment(Payment payment, Offer offer) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }

        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid payment amount.");
        }

        if (paymentRepository.exists(payment.getPaymentId())) {
            throw new IllegalArgumentException(
                "Payment ID already exists."
            );
        }

        if (payment instanceof UpiPayment) {

            UpiPayment upi = (UpiPayment) payment;

            if (!upi.validateUpi()) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new IllegalArgumentException("Invalid UPI ID.");
            }

        } else if (payment instanceof CardPayment) {

            CardPayment card = (CardPayment) payment;

            if (!card.validateCard()) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new IllegalArgumentException("Invalid card number.");
            }

        } else if (payment instanceof NetBankingPayment) {

            NetBankingPayment bank = (NetBankingPayment) payment;

            if (!bank.validateAccount()) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new IllegalArgumentException(
                    "Invalid bank account number."
                );
            }
        }

        double finalAmount =
            pricingService.calculateFinalAmount(
                payment.getAmount(), offer
            );

        payment.setFinalAmount(finalAmount);

        // Simulated payment processing.
        // Real bank/payment gateway integration is not included.

        payment.setStatus(PaymentStatus.SUCCESS);

        paymentRepository.save(payment);

        return payment;
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getCustomerPayments(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }
}
