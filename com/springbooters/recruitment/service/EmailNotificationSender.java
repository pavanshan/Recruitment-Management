package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.NotificationType;

import java.util.UUID;

/** Email implementation of the notification sender abstraction. */
public class EmailNotificationSender implements NotificationSender {

    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "465";

    @Override
    public NotificationLog send(NotificationType type, String contactInfo, String message) {
        NotificationLog log = new NotificationLog();
        log.setNotificationId("EMAIL-" + UUID.randomUUID());
        log.setNotificationType(type.name());
        log.setContactInfoUsed(contactInfo);
        log.setStatusAlert("[EMAIL] " + message);
        log.validate();

        if (isRealEmailEnabled()) {
            sendRealEmail(contactInfo, type, message);
        } else {
            System.out.println("Email notification sent to " + contactInfo + ": " + message);
        }
        return log;
    }

    private boolean isRealEmailEnabled() {
        return "true".equalsIgnoreCase(System.getenv("SMTP_ENABLED"));
    }

    private void sendRealEmail(String contactInfo, NotificationType type, String message) {
        String host = System.getenv().getOrDefault("SMTP_HOST", DEFAULT_SMTP_HOST);
        int port = Integer.parseInt(System.getenv().getOrDefault("SMTP_PORT", DEFAULT_SMTP_PORT));
        String user = System.getenv("SMTP_USER");
        String password = System.getenv("SMTP_PASSWORD");
        String from = System.getenv().getOrDefault("SMTP_FROM", user);

        if (user == null || user.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || from == null || from.trim().isEmpty()) {
            throw new IllegalStateException("SMTP_USER, SMTP_PASSWORD and SMTP_FROM are required for real email");
        }

        try {
            SmtpEmailClient client = new SmtpEmailClient(host, port, user, password, from);
            client.send(contactInfo, "Recruitment Update - " + type.name(), message);
            System.out.println("Real email sent to " + contactInfo + ": " + message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send real email: " + ex.getMessage(), ex);
        }
    }
}
