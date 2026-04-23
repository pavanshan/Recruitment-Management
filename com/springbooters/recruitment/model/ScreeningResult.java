package com.springbooters.recruitment.model;

/**
 * Stores the outcome of the automated screening process for an application.
 *
 * GRASP - Information Expert: ScreeningResult knows its own score and
 * shortlist status, and can answer whether a candidate passed screening.
 */
public class ScreeningResult {

    private String applicationId;
    private int score;               // Composite score 0–100
    private int ranking;             // Rank among all applicants for the same job
    private String shortlistStatus;  // SHORTLISTED, REJECTED, PENDING
    private String remarks;          // Audit string [Resume: X][Exp: Y][Skills: Z]

    public ScreeningResult() {}

    /** Returns true if this candidate has been shortlisted. */
    public boolean isShortlisted() {
        return "SHORTLISTED".equalsIgnoreCase(shortlistStatus);
    }

    /** Returns true if candidate is rejected */
    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(shortlistStatus);
    }

    /**
     * Basic validation for screening result integrity.
     */
    public void validate() {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            throw new IllegalStateException("Application ID is required");
        }

        if (score < 0 || score > 100) {
            throw new IllegalStateException("Score must be between 0 and 100");
        }

        if (ranking < 0) {
            throw new IllegalStateException("Ranking cannot be negative");
        }

        ShortlistStatus.from(shortlistStatus);
    }

    public String getApplicationId()                        { return applicationId; }
    public void setApplicationId(String applicationId)      { this.applicationId = applicationId; }

    public int getScore()                                   { return score; }

    public void setScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        this.score = score;
    }

    public int getRanking()                                 { return ranking; }

    public void setRanking(int ranking) {
        if (ranking < 0) {
            throw new IllegalArgumentException("Ranking cannot be negative");
        }
        this.ranking = ranking;
    }

    public String getShortlistStatus()                      { return shortlistStatus; }

    public void setShortlistStatus(String shortlistStatus) {
        this.shortlistStatus = ShortlistStatus.from(shortlistStatus).name();
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    @Override
    public String toString() {
        return "ScreeningResult{applicationId='" + applicationId
                + "', score=" + score
                + ", ranking=" + ranking
                + ", status='" + shortlistStatus
                + "', remarks='" + remarks + "'}";
    }
}
