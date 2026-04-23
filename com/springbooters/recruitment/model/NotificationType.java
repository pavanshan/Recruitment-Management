package com.springbooters.recruitment.model;

/** Auditable notification categories sent by the recruitment subsystem. */
public enum NotificationType {
    APPLICATION_RECEIVED,
    INTERVIEW_SCHEDULED,
    OFFER_SENT,
    STATUS_UPDATE;

    public static NotificationType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type is required");
        }
        try {
            return NotificationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid notification type: " + value, ex);
        }
    }
}
