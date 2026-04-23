package com.springbooters.recruitment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Creational Pattern - Builder.
 *
 * Builds JobPosting objects without a long constructor and validates the
 * finished object before it is used by services.
 */
public class JobPostingBuilder {

    private final JobPosting jobPosting = new JobPosting();

    public JobPostingBuilder withJobId(String jobId) {
        jobPosting.setJobId(jobId);
        return this;
    }

    public JobPostingBuilder withTitle(String title) {
        jobPosting.setTitle(title);
        return this;
    }

    public JobPostingBuilder withDepartment(String department) {
        jobPosting.setDepartment(department);
        return this;
    }

    public JobPostingBuilder withDescription(String description) {
        jobPosting.setDescription(description);
        return this;
    }

    public JobPostingBuilder withSalary(BigDecimal salary) {
        jobPosting.setSalary(salary);
        return this;
    }

    public JobPostingBuilder withStatus(JobStatus status) {
        jobPosting.setStatus(status.name());
        return this;
    }

    public JobPostingBuilder withPlatform(String platformName) {
        jobPosting.setPlatformName(platformName);
        return this;
    }

    public JobPostingBuilder withChannel(ChannelType channelType) {
        jobPosting.setChannelType(channelType.name());
        return this;
    }

    public JobPostingBuilder withPostedDate(LocalDate postedDate) {
        jobPosting.setPostedDate(postedDate);
        return this;
    }

    public JobPostingBuilder withExpiryDate(LocalDate expiryDate) {
        jobPosting.setExpiryDate(expiryDate);
        return this;
    }

    public JobPostingBuilder withExperience(int experience) {
        jobPosting.setExperience(experience);
        return this;
    }

    public JobPostingBuilder withMinScreeningScore(int score) {
        jobPosting.setMinScreeningScore(score);
        return this;
    }

    public JobPostingBuilder withRequiredSkills(String skills) {
        jobPosting.setRequiredSkills(skills);
        return this;
    }

    public JobPosting build() {
        jobPosting.validate();
        return jobPosting;
    }
}
