package com.springbooters.recruitment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a job opening in the Recruitment Management System.
 *
 * GRASP - Information Expert: JobPosting holds all data related to a job,
 * and is best placed to answer queries about it (e.g., isExpired()).
 *
 * SOLID - Single Responsibility: This class only models job posting data.
 * Business logic lives in JobPostingService.
 *
 * Built via JobPostingBuilder (Creational - Builder Pattern).
 */
public class JobPosting {

    private String jobId;
    private String title;
    private String department;
    private String description;
    private BigDecimal salary;
    private String status;          // DRAFT, ACTIVE, EXPIRED, CLOSED
    private String platformName;    // e.g., LinkedIn, Naukri, Internal
    private String channelType;     // EXTERNAL, INTERNAL
    private LocalDate postedDate;
    private LocalDate expiryDate;
    private int experience;
    private int minScreeningScore = 60;
    private String requiredSkills;

    public JobPosting() {}

    /** Checks if this job posting is expired based on the expiry date. */
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    /** Checks if this posting is currently accepting applications. */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status) && !isExpired();
    }

    /**
     * Basic validation for job posting integrity.
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Job title is required");
        }

        if (department == null || department.trim().isEmpty()) {
            throw new IllegalStateException("Department is required");
        }

        JobStatus.from(status);

        if (expiryDate != null && postedDate != null && expiryDate.isBefore(postedDate)) {
            throw new IllegalStateException("Expiry date cannot be before posted date");
        }
    }

    public String getJobId()        { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle()        { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment()   { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription()  { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getSalary()   { return salary; }

    public void setSalary(BigDecimal salary) {
        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    public String getStatus()       { return status; }

    public void setStatus(String status) {
        this.status = JobStatus.from(status).name();
    }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getChannelType()  { return channelType; }

    public void setChannelType(String channelType) {
        this.channelType = ChannelType.from(channelType).name();
    }

    public LocalDate getPostedDate(){ return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public LocalDate getExpiryDate(){ return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getMinScreeningScore() { return minScreeningScore; }
    public void setMinScreeningScore(int minScreeningScore) { this.minScreeningScore = minScreeningScore; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    @Override
    public String toString() {
        return "JobPosting{jobId='" + jobId
                + "', title='" + title
                + "', status='" + status
                + "', platform='" + platformName + "'}";
    }
}
