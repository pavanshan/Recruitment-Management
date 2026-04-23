package com.springbooters.recruitment.model;

/** Supported interview modes/rounds. */
public enum InterviewType {
    PHONE,
    VIDEO,
    IN_PERSON,
    TECHNICAL;

    public static InterviewType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Interview type is required");
        }
        try {
            return InterviewType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid interview type: " + value, ex);
        }
    }
}
