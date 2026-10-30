package models;

public class CardPayment extends Payment {

    private String cardHolderName;
    private String cardNumber;

    public CardPayment(String paymentId, String customerId,
                       double amount, String cardHolderName,
                       String cardNumber) {

        super(paymentId, customerId, amount, PaymentMethod.CARD);

        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public boolean validateCard() {
        return cardNumber != null &&
               cardNumber.matches("\\d{16}");
    }

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() != 16) {
            return "Invalid card";
        }

        return "************" +
               cardNumber.substring(12);
    }
}
