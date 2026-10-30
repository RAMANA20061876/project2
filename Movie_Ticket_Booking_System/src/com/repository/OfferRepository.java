package repository;

import models.Offer;
import java.util.*;

public class OfferRepository {

    private Map<String, Offer> offers = new HashMap<>();

    public void save(Offer offer) {
        offers.put(offer.getOfferId(), offer);
    }

    public Offer findById(String offerId) {
        return offers.get(offerId);
    }

    public List<Offer> findAll() {
        return new ArrayList<>(offers.values());
    }

    public void delete(String offerId) {
        offers.remove(offerId);
    }
}
