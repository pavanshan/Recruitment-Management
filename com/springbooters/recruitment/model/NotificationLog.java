package com.springbooters.recruitment.model;

import java.time.LocalDateTime;

/**
 * Audit log entry for every notification sent by the Notification Manager.
 * Records what was sent, to whom, and when — for compliance and traceability.
 *
 * SOLID - Single Responsibility: Only stores notification metadata.
 * Sending logic lives in NotificationFactory + NotificationService.
 */
public class NotificationLog {

    private String notificationId;

    /** Type of notification: APPLICATION_RECEIVED, INTERVIEW_SCHEDULED, OFFER_SENT, STATUS_UPDATE */
    private String notificationType;

    private String sentAds;           // Job ad content if this was an advertisement notification
    private String statusAlert;       // Message body for status change alerts
    private String contactInfoUsed;   // The email/phone the notification was sent to
    private LocalDateTime sentTimestamp;

    public NotificationLog() {
        this.sentTimestamp = LocalDateTime.now(); // default timestamp
    }

    /**
     * Basic validation for logging integrity.
     */
    public void validate() {
        NotificationType.from(notificationType);

        if (contactInfoUsed == null || contactInfoUsed.trim().isEmpty()) {
            throw new IllegalStateException("Contact information is required");
        }
    }

    public String getNotificationId()                           { return notificationId; }
    public void setNotificationId(String notificationId)        { this.notificationId = notificationId; }

    public String getNotificationType()                         { return notificationType; }

    public void setNotificationType(String notificationType) {
        this.notificationType = NotificationType.from(notificationType).name();
    }

    public String getSentAds()                                  { return sentAds; }
    public void setSentAds(String sentAds)                      { this.sentAds = sentAds; }

    public String getStatusAlert()                              { return statusAlert; }
    public void setStatusAlert(String statusAlert)              { this.statusAlert = statusAlert; }

    public String getContactInfoUsed()                          { return contactInfoUsed; }
    public void setContactInfoUsed(String contactInfoUsed)      { this.contactInfoUsed = contactInfoUsed; }

    public LocalDateTime getSentTimestamp()                     { return sentTimestamp; }

    public void setSentTimestamp(LocalDateTime sentTimestamp) {
        this.sentTimestamp = (sentTimestamp != null) ? sentTimestamp : LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "NotificationLog{notificationId='" + notificationId
                + "', type='" + notificationType
                + "', contact='" + contactInfoUsed
                + "', timestamp=" + sentTimestamp + "}";
    }
}
