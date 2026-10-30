package repository;

import models.Refund;
import java.util.*;

public class RefundRepository {

    private Map<String, Refund> refunds = new HashMap<>();

    public void save(Refund refund) {
        refunds.put(refund.getRefundId(), refund);
    }

    public Refund findById(String refundId) {
        return refunds.get(refundId);
    }

    public List<Refund> findAll() {
        return new ArrayList<>(refunds.values());
    }

    public List<Refund> findByPaymentId(String paymentId) {

        List<Refund> result = new ArrayList<>();

        for (Refund refund : refunds.values()) {
            if (refund.getPaymentId().equals(paymentId)) {
                result.add(refund);
            }
        }

        return result;
    }
}
