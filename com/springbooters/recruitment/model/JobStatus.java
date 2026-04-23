package com.springbooters.recruitment.model;

/** Valid lifecycle states for a job posting. */
public enum JobStatus {
    DRAFT,
    APPROVED,
    ACTIVE,
    EXPIRED,
    CLOSED;

    public static JobStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Job status is required");
        }
        try {
            return JobStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid job status: " + value, ex);
        }
    }
}
