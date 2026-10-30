package cli;

import models.*;
import services.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class PaymentCLI {

    private Scanner scanner;

    private PaymentService paymentService;
    private OfferService offerService;
    private RefundService refundService;

    public PaymentCLI(PaymentService paymentService,
                      OfferService offerService,
                      RefundService refundService) {

        this.scanner = new Scanner(System.in);

        this.paymentService = paymentService;
        this.offerService = offerService;
        this.refundService = refundService;
    }

    public void start() {

        int choice;

        do {
            System.out.println("\n=================================");
            System.out.println("       PAYMENT SYSTEM");
            System.out.println("=================================");
            System.out.println("1. Make Payment");
            System.out.println("2. View All Payments");
            System.out.println("3. Search Payment");
            System.out.println("4. View Offers");
            System.out.println("5. Create Offer");
            System.out.println("6. Apply Refund");
            System.out.println("7. View All Refunds");
            System.out.println("8. Customer Payment History");
            System.out.println("9. Exit");
            System.out.println("=================================");

            choice = readInt("Enter your choice: ");

            try {
                switch (choice) {

                    case 1:
                        makePayment();
                        break;

                    case 2:
                        viewAllPayments();
                        break;

                    case 3:
                        searchPayment();
                        break;

                    case 4:
                        viewOffers();
                        break;

                    case 5:
                        createOffer();
                        break;

                    case 6:
                        applyRefund();
                        break;

                    case 7:
                        viewRefunds();
                        break;

                    case 8:
                        customerHistory();
                        break;

                    case 9:
                        System.out.println("Thank you for using Payment System!");
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());

            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }

        } while (choice != 9);
    }

    // 1. Make Payment

    private void makePayment() {

        System.out.println("\n--- MAKE PAYMENT ---");

        String customerId = readString("Enter Customer ID: ");

        double amount = readDouble("Enter Amount: ");

        System.out.println("\nSelect Payment Method:");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Net Banking");

        int method = readInt("Enter choice: ");

        String paymentId = "PAY-" +
            UUID.randomUUID().toString().substring(0, 8);

        Payment payment;

        switch (method) {

            case 1:

                String upiId = readString("Enter UPI ID: ");

                payment = new UpiPayment(
                    paymentId,
                    customerId,
                    amount,
                    upiId
                );

                break;

            case 2:

                String holder = readString("Enter Card Holder Name: ");

                String cardNumber = readString("Enter 16-digit Card Number: ");

                payment = new CardPayment(
                    paymentId,
                    customerId,
                    amount,
                    holder,
                    cardNumber
                );

                break;

            case 3:

                String bankName = readString("Enter Bank Name: ");

                String accountNumber =
                    readString("Enter Account Number: ");

                payment = new NetBankingPayment(
                    paymentId,
                    customerId,
                    amount,
                    bankName,
                    accountNumber
                );

                break;

            default:

                System.out.println("Invalid payment method.");
                return;
        }

        System.out.println("\nAvailable Offers:");

        List<Offer> offers = offerService.getAllOffers();

        for (Offer offer : offers) {
            if (offer.getStatus() == OfferStatus.ACTIVE) {
                System.out.println(offer);
            }
        }

        String offerId = readString(
            "\nEnter Offer ID (Press Enter to skip): "
        );

        Offer selectedOffer = null;

        if (!offerId.isEmpty()) {

            selectedOffer = offerService.getOffer(offerId);

            if (selectedOffer == null) {
                System.out.println("Offer not found.");
                return;
            }
        }

        Payment result = paymentService.processPayment(
            payment,
            selectedOffer
        );

        System.out.println("\nPayment Successful!");
        System.out.println(result);
    }

    // 2. View All Payments

    private void viewAllPayments() {

        System.out.println("\n--- ALL PAYMENTS ---");

        List<Payment> payments = paymentService.getAllPayments();

        if (payments.isEmpty()) {
            System.out.println("No payments found.");
            return;
        }

        for (Payment payment : payments) {
            System.out.println(payment);
            System.out.println("------------------------");
        }
    }

    // 3. Search Payment

    private void searchPayment() {

        String paymentId = readString("Enter Payment ID: ");

        Payment payment = paymentService.getPayment(paymentId);

        if (payment == null) {
            System.out.println("Payment not found.");
        } else {
            System.out.println(payment);
        }
    }

    // 4. View Offers

    private void viewOffers() {

        System.out.println("\n--- AVAILABLE OFFERS ---");

        List<Offer> offers = offerService.getAllOffers();

        if (offers.isEmpty()) {
            System.out.println("No offers available.");
            return;
        }

        for (Offer offer : offers) {
            System.out.println(offer);
            System.out.println("------------------------");
        }
    }

    // 5. Create Offer

    private void createOffer() {

        System.out.println("\n--- CREATE OFFER ---");

        String offerId = readString("Enter Offer ID: ");

        String offerName = readString("Enter Offer Name: ");

        double discount = readDouble(
            "Enter Discount Percentage: "
        );

        double minimumAmount = readDouble(
            "Enter Minimum Purchase Amount: "
        );

        String expiry = readString(
            "Enter Expiry Date (YYYY-MM-DD): "
        );

        LocalDate expiryDate = LocalDate.parse(expiry);

        offerService.createOffer(
            offerId,
            offerName,
            discount,
            minimumAmount,
            expiryDate
        );

        System.out.println("Offer created successfully!");
    }

    // 6. Apply Refund

    private void applyRefund() {

        System.out.println("\n--- APPLY REFUND ---");

        String paymentId = readString("Enter Payment ID: ");

        String reason = readString("Enter Refund Reason: ");

        Refund refund = refundService.processRefund(
            paymentId,
            reason
        );

        System.out.println("\nRefund processed successfully!");
        System.out.println(refund);
    }

    // 7. View Refunds

    private void viewRefunds() {

        System.out.println("\n--- ALL REFUNDS ---");

        List<Refund> refunds = refundService.getAllRefunds();

        if (refunds.isEmpty()) {
            System.out.println("No refunds found.");
            return;
        }

        for (Refund refund : refunds) {
            System.out.println(refund);
            System.out.println("------------------------");
        }
    }

    // 8. Customer Payment History

    private void customerHistory() {

        String customerId = readString("Enter Customer ID: ");

        List<Payment> payments =
            paymentService.getCustomerPayments(customerId);

        if (payments.isEmpty()) {
            System.out.println("No payment history found.");
            return;
        }

        System.out.println("\n--- CUSTOMER PAYMENT HISTORY ---");

        for (Payment payment : payments) {
            System.out.println(payment);
            System.out.println("------------------------");
        }
    }

    // Input helper methods

    private String readString(String message) {

        System.out.print(message);

        return scanner.nextLine().trim();
    }

    private int readInt(String message) {

        while (true) {
            try {
                System.out.print(message);

                return Integer.parseInt(
                    scanner.nextLine().trim()
                );

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private double readDouble(String message) {

        while (true) {
            try {
                System.out.print(message);

                double value = Double.parseDouble(
                    scanner.nextLine().trim()
                );

                if (!Double.isFinite(value)) {
                    System.out.println("Enter a valid amount.");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}
