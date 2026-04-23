package com.springbooters.recruitment.service;

/**
 * Creational Pattern - Factory.
 *
 * Centralizes the decision of which notification sender to create.
 */
public class NotificationFactory {

    public NotificationSender createSender(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification channel is required");
        }

        String normalized = channel.trim().toUpperCase();
        if ("EMAIL".equals(normalized)) {
            return new EmailNotificationSender();
        }
        if ("SMS".equals(normalized)) {
            return new SmsNotificationSender();
        }
        throw new IllegalArgumentException("Unsupported notification channel: " + channel);
    }
}
