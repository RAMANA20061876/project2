package models;

import java.time.LocalDate;

public class Offer {

    private String offerId;
    private String offerName;
    private double discountPercentage;
    private double minimumAmount;
    private OfferStatus status;
    private LocalDate expiryDate;

    public Offer(String offerId, String offerName,
                 double discountPercentage,
                 double minimumAmount,
                 LocalDate expiryDate) {

        this.offerId = offerId;
        this.offerName = offerName;
        this.discountPercentage = discountPercentage;
        this.minimumAmount = minimumAmount;
        this.expiryDate = expiryDate;
        this.status = OfferStatus.ACTIVE;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getOfferName() {
        return offerName;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getMinimumAmount() {
        return minimumAmount;
    }

    public OfferStatus getStatus() {
        if (LocalDate.now().isAfter(expiryDate)) {
            return OfferStatus.EXPIRED;
        }
        return status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "\nOffer ID: " + offerId +
               "\nOffer Name: " + offerName +
               "\nDiscount: " + discountPercentage + "%" +
               "\nMinimum Amount: Rs." + minimumAmount +
               "\nExpiry Date: " + expiryDate +
               "\nStatus: " + getStatus();
    }
}
