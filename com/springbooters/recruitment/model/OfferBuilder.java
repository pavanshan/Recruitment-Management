package com.springbooters.recruitment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Creational Pattern - Builder.
 *
 * Offer has several optional fields, so a builder keeps creation readable and
 * avoids constructor overloads.
 */
public class OfferBuilder {

    private final Offer offer = new Offer();

    public OfferBuilder withOfferId(String offerId) {
        offer.setOfferId(offerId);
        return this;
    }

    public OfferBuilder forCandidate(String candidateId) {
        offer.setCandidateId(candidateId);
        return this;
    }

    public OfferBuilder withDetails(String offerDetails) {
        offer.setOfferDetails(offerDetails);
        return this;
    }

    public OfferBuilder withSalary(BigDecimal salary) {
        offer.setSalary(salary);
        return this;
    }

    public OfferBuilder startingOn(LocalDate startDate) {
        offer.setStartDate(startDate);
        return this;
    }

    public OfferBuilder expiringOn(LocalDate expiryDate) {
        offer.setExpiryDate(expiryDate);
        return this;
    }

    public OfferBuilder withStatus(OfferStatus status) {
        offer.setStatus(status.name());
        return this;
    }

    public Offer build() {
        offer.validate();
        return offer;
    }
}
