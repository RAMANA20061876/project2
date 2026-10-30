package services;

import models.*;
import repository.OfferRepository;

import java.time.LocalDate;
import java.util.List;

public class OfferService {

    private OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public void createOffer(String offerId,
                            String offerName,
                            double discount,
                            double minimumAmount,
                            LocalDate expiryDate) {

        if (offerId == null || offerId.trim().isEmpty() ||
            offerName == null || offerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid offer details.");
        }

        if (offerRepository.findById(offerId) != null) {
            throw new IllegalArgumentException("Offer ID already exists.");
        }

        if (discount <= 0 || discount > 100) {
            throw new IllegalArgumentException(
                "Discount must be between 0 and 100."
            );
        }

        if (minimumAmount < 0) {
            throw new IllegalArgumentException(
                "Minimum amount cannot be negative."
            );
        }

        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid expiry date.");
        }

        Offer offer = new Offer(
            offerId,
            offerName,
            discount,
            minimumAmount,
            expiryDate
        );

        offerRepository.save(offer);
    }

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Offer getOffer(String offerId) {
        return offerRepository.findById(offerId);
    }

    public void deactivateOffer(String offerId) {

        Offer offer = offerRepository.findById(offerId);

        if (offer == null) {
            throw new IllegalArgumentException("Offer not found.");
        }

        offer.setStatus(OfferStatus.INACTIVE);
    }
}
