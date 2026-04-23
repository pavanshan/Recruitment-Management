package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.NotificationType;

import java.util.UUID;

/** SMS implementation of the notification sender abstraction. */
public class SmsNotificationSender implements NotificationSender {

    @Override
    public NotificationLog send(NotificationType type, String contactInfo, String message) {
        NotificationLog log = new NotificationLog();
        log.setNotificationId("SMS-" + UUID.randomUUID());
        log.setNotificationType(type.name());
        log.setContactInfoUsed(contactInfo);
        log.setStatusAlert("[SMS] " + message);
        log.validate();

        System.out.println("SMS notification sent to " + contactInfo + ": " + message);
        return log;
    }
}
