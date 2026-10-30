package services;

import models.Offer;

public class PricingService {

    public double calculateFinalAmount(double amount, Offer offer) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero."
            );
        }

        if (offer == null) {
            return amount;
        }

        if (!offer.getStatus().equals(models.OfferStatus.ACTIVE)) {
            throw new IllegalArgumentException(
                "Offer is not active."
            );
        }

        if (amount < offer.getMinimumAmount()) {
            throw new IllegalArgumentException(
                "Minimum purchase amount not reached."
            );
        }

        double discount =
            amount * offer.getDiscountPercentage() / 100;

        double finalAmount = amount - discount;

        return Math.round(finalAmount * 100.0) / 100.0;
    }
}
