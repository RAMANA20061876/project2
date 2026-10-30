package models;

import java.time.LocalDateTime;

public class Payment {

    private String paymentId;
    private String customerId;
    private double amount;
    private double finalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;

    public Payment(String paymentId, String customerId,
                   double amount, PaymentMethod paymentMethod) {

        this.paymentId = paymentId;
        this.customerId = customerId;
        this.amount = amount;
        this.finalAmount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.paymentDate = LocalDateTime.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getAmount() {
        return amount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    @Override
    public String toString() {
        return "\nPayment ID: " + paymentId +
               "\nCustomer ID: " + customerId +
               "\nAmount: Rs." + amount +
               "\nFinal Amount: Rs." + finalAmount +
               "\nPayment Method: " + paymentMethod +
               "\nStatus: " + status +
               "\nDate: " + paymentDate;
    }
}
