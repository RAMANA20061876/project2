package models;

public class NetBankingPayment extends Payment {

    private String bankName;
    private String accountNumber;

    public NetBankingPayment(String paymentId,
                             String customerId,
                             double amount,
                             String bankName,
                             String accountNumber) {

        super(paymentId, customerId,
              amount, PaymentMethod.NET_BANKING);

        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean validateAccount() {
        return accountNumber != null &&
               accountNumber.matches("\\d{9,18}");
    }
}
