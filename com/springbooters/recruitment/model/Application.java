package com.springbooters.recruitment.model;

import java.time.LocalDate;

/**
 * Represents a candidate's application for a specific job posting.
 *
 * GRASP - Creator: Application is created by ApplicationService, which
 * already holds the initializing data (candidateId + jobId).
 *
 * SOLID - Single Responsibility: Links candidate to job; status tracking
 * is delegated to ApplicationStatus.
 */
public class Application {

    private String applicationId;
    private String candidateId;
    private String jobId;
    private LocalDate dateApplied;

    public Application() {}

    // Optional convenience constructor (useful in services)
    public Application(String applicationId, String candidateId, String jobId, LocalDate dateApplied) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.dateApplied = dateApplied;
    }

    public String getApplicationId()                    { return applicationId; }
    public void setApplicationId(String applicationId)  { this.applicationId = applicationId; }

    public String getCandidateId()                      { return candidateId; }
    public void setCandidateId(String candidateId)      { this.candidateId = candidateId; }

    public String getJobId()                            { return jobId; }
    public void setJobId(String jobId)                  { this.jobId = jobId; }

    public LocalDate getDateApplied()                   { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied)   { this.dateApplied = dateApplied; }

    /**
     * Basic validation to ensure required fields are present.
     * Helps avoid invalid application objects in the system.
     */
    public void validate() {
        if (candidateId == null || candidateId.trim().isEmpty()
                || jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalStateException("Application must have candidateId and jobId");
        }
        if (dateApplied == null) {
            throw new IllegalStateException("Application date is required");
        }
    }

    @Override
    public String toString() {
        return "Application{applicationId='" + applicationId
               + "', candidateId='" + candidateId
               + "', jobId='" + jobId
               + "', dateApplied=" + dateApplied + "}";
    }
}
