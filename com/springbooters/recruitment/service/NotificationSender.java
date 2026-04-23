package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.NotificationType;

/**
 * SOLID - Dependency Inversion: Services depend on this abstraction instead of
 * concrete email/SMS implementations.
 */
public interface NotificationSender {

    NotificationLog send(NotificationType type, String contactInfo, String message);
}
