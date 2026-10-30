package models;

import java.math.BigDecimal;

public class RefundItem {
    private int refundId;
    private int bookingId;
    private int paymentId;      // 0 means the booking has no successful payment
    private BigDecimal amount;

    public RefundItem(int bookingId, int paymentId, BigDecimal amount) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public int getRefundId() { return refundId; }
    public void setRefundId(int refundId) { this.refundId = refundId; }

    public int getBookingId() { return bookingId; }
    public int getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
}