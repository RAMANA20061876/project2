package models;

import java.time.LocalDateTime;

public class Refund {

    private String refundId;
    private String paymentId;
    private double refundAmount;
    private String reason;
    private LocalDateTime refundDate;

    public Refund(String refundId, String paymentId,
                  double refundAmount, String reason) {

        this.refundId = refundId;
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.refundDate = LocalDateTime.now();
    }

    public String getRefundId() {
        return refundId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    @Override
    public String toString() {
        return "\nRefund ID: " + refundId +
               "\nPayment ID: " + paymentId +
               "\nRefund Amount: Rs." + refundAmount +
               "\nReason: " + reason +
               "\nRefund Date: " + refundDate;
    }
}
