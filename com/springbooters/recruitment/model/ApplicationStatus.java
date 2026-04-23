package com.springbooters.recruitment.model;

import java.time.LocalDateTime;

/**
 * Tracks the current stage and full history of an application's progression.
 *
 * Pipeline stages: APPLIED → SCREENED → SHORTLISTED → INTERVIEW_SCHEDULED → INTERVIEW_COMPLETED → OFFERED → HIRED / REJECTED
 *
 * GRASP - Information Expert: ApplicationStatus knows the current stage
 * and history; it is the right place to query an application's pipeline position.
 *
 * SOLID - Single Responsibility: Handles only stage/history tracking,
 * separate from the Application entity itself.
 */
public class ApplicationStatus {

    private String applicationId;

    /**
     * Current stage of the pipeline.
     * Valid values: APPLIED, SCREENED, SHORTLISTED, INTERVIEW_SCHEDULED, INTERVIEW_COMPLETED, OFFERED, HIRED, REJECTED
     */
    private String currentStage;

    /** Semicolon-delimited history of all past stages with timestamps. */
    private String history;

    /** Timestamp of the last stage update. */
    private LocalDateTime timestamp;

    public ApplicationStatus() {
        this.timestamp = LocalDateTime.now();
    }

    public String getApplicationId()                        { return applicationId; }
    public void setApplicationId(String applicationId)      { this.applicationId = applicationId; }

    public String getCurrentStage()                         { return currentStage; }
    public void setCurrentStage(String currentStage)       { this.currentStage = currentStage; }

    /**
     * Controlled update of stage + history.
     * Prevents inconsistent manual updates.
     */
    public void updateStage(String newStage) {
        ApplicationStage nextStage = ApplicationStage.from(newStage);
        ApplicationStage previousStage = currentStage == null ? null : ApplicationStage.from(currentStage);

        if (nextStage.name().equals(this.currentStage)) {
            throw new IllegalStateException("Duplicate stage transition ignored: already in " + currentStage);
        }

        if (!isTransitionAllowed(previousStage, nextStage)) {
            throw new IllegalStateException("Invalid application stage transition from "
                    + previousStage + " to " + nextStage);
        }

        this.currentStage = nextStage.name();
        this.timestamp = LocalDateTime.now();

        String transition = (previousStage == null ? "" : previousStage.name() + " -> ") + nextStage.name();
        String entry = transition + " @ " + timestamp;

        if (history == null || history.isEmpty()) {
            history = entry;
        } else {
            history += " ; " + entry;
        }
    }

    public String getHistory()                              { return history; }
    public void setHistory(String history)                  { this.history = history; }

    public LocalDateTime getTimestamp()                     { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp)       { this.timestamp = timestamp; }

    /**
     * Checks if the application has ever reached the specified milestone.
     * This is used for GUI persistence logic to ensure candidates only show up
     * in sections they actually visited.
     */
    public boolean hasReached(ApplicationStage milestone) {
        if (currentStage == null) return false;
        
        ApplicationStage current = ApplicationStage.from(currentStage);
        
        // Positive terminal case: If HIRED, everything up to HIRED was reached
        if (current == ApplicationStage.HIRED && milestone.ordinal() <= current.ordinal()) {
            return true;
        }

        // Check history string for the stage name to see if it was ever attained
        return history != null && history.contains(milestone.name());
    }

    private boolean isTransitionAllowed(ApplicationStage previousStage, ApplicationStage nextStage) {
        if (previousStage == null) {
            return true;
        }

        // Terminal State Guard: No transitions allowed away from HIRED or REJECTED
        if (previousStage == ApplicationStage.HIRED || previousStage == ApplicationStage.REJECTED) {
            return false;
        }

        // Generic Reject path: Any non-terminal state can move to REJECTED
        if (nextStage == ApplicationStage.REJECTED) {
            return true;
        }

        switch (previousStage) {
            case APPLIED:
                return nextStage == ApplicationStage.SCREENED;
            case SCREENED:
                return nextStage == ApplicationStage.SHORTLISTED;
            case SHORTLISTED:
                return nextStage == ApplicationStage.INTERVIEW_SCHEDULED || nextStage == ApplicationStage.INTERVIEW_COMPLETED;
            case INTERVIEW_SCHEDULED:
                return nextStage == ApplicationStage.INTERVIEW_COMPLETED;
            case INTERVIEW_COMPLETED:
                return nextStage == ApplicationStage.OFFERED;
            case OFFERED:
                return nextStage == ApplicationStage.HIRED;
            default:
                return false;
        }
    }
}
