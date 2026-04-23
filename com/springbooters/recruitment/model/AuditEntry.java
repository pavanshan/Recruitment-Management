package com.springbooters.recruitment.model;

import java.time.LocalDateTime;

/**
 * Audit log entry for tracking system actions and actor details.
 * Fulfills Requirement 8 for persistent audit logging.
 */
public class AuditEntry {

    private String entryId;
    private LocalDateTime timestamp;
    private String action;
    private String actor;
    private String details;

    public AuditEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditEntry(String action, String actor, String details) {
        this();
        this.action = action;
        this.actor = actor;
        this.details = details;
    }

    public void validate() {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalStateException("Audit action is required");
        }
        if (actor == null || actor.trim().isEmpty()) {
            throw new IllegalStateException("Audit actor is required");
        }
    }

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + actor + " - " + action + ": " + details;
    }
}
