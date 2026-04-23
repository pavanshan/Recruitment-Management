package com.springbooters.recruitment.model;

/** Screening outcomes produced by resume/application evaluation. */
public enum ShortlistStatus {
    SHORTLISTED,
    REJECTED,
    PENDING;

    public static ShortlistStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Shortlist status is required");
        }
        try {
            return ShortlistStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid shortlist status: " + value, ex);
        }
    }
}
