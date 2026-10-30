package repository;

import models.Payment;
import java.util.*;

public class PaymentRepository {

    private Map<String, Payment> payments = new HashMap<>();

    public void save(Payment payment) {
        payments.put(payment.getPaymentId(), payment);
    }

    public Payment findById(String paymentId) {
        return payments.get(paymentId);
    }

    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    public List<Payment> findByCustomerId(String customerId) {

        List<Payment> result = new ArrayList<>();

        for (Payment payment : payments.values()) {
            if (payment.getCustomerId().equals(customerId)) {
                result.add(payment);
            }
        }

        return result;
    }

    public boolean exists(String paymentId) {
        return payments.containsKey(paymentId);
    }
}
