package com.springbooters.recruitment.model;

/** Controlled states for offer management. */
public enum OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED;

    public static OfferStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Offer status is required");
        }
        try {
            return OfferStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid offer status: " + value, ex);
        }
    }
}
