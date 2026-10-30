package models;

public class UpiPayment extends Payment {

    private String upiId;

    public UpiPayment(String paymentId, String customerId,
                      double amount, String upiId) {

        super(paymentId, customerId, amount, PaymentMethod.UPI);
        this.upiId = upiId;
    }

    public String getUpiId() {
        return upiId;
    }

    public boolean validateUpi() {
        return upiId != null &&
               upiId.matches("[a-zA-Z0-9._-]+@[a-zA-Z]+");
    }
}
