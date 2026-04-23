package com.springbooters.recruitment.model;

/**
 * Controlled recruitment pipeline stages.
 *
 * GRASP - Protected Variations: Any class that needs a stage depends on this
 * enum instead of hard-coded strings scattered throughout the subsystem.
 */
public enum ApplicationStage {
    APPLIED,
    SCREENED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    INTERVIEW_COMPLETED,
    OFFERED,
    HIRED,
    REJECTED;

    public static ApplicationStage from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Application stage is required");
        }
        try {
            return ApplicationStage.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid application stage: " + value, ex);
        }
    }

    public boolean isTerminal() {
        return this == HIRED || this == REJECTED;
    }
}
