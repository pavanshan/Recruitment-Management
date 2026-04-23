package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Application;
import com.springbooters.recruitment.model.ApplicationStatus;
import com.springbooters.recruitment.model.AuditEntry;
import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.EmployeeRecord;
import com.springbooters.recruitment.model.InterviewSchedule;
import com.springbooters.recruitment.model.JobPosting;
import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.Offer;
import com.springbooters.recruitment.model.ScreeningResult;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Integration interface exposed to other HRMS subsystems.
 *
 * Other modules such as Employee Database, Onboarding, Payroll and Analytics
 * should depend on this interface instead of depending directly on internal
 * recruitment implementation classes.
 *
 * GRASP - Indirection: This interface decouples external HRMS modules from
 * recruitment internals.
 * SOLID - Dependency Inversion: High-level HRMS modules depend on an
 * abstraction, not a concrete recruitment facade.
 */
public interface RecruitmentIntegration {

    void addCandidate(Candidate candidate);

    void publishJob(JobPosting jobPosting);

    /** Records a new job requisition in DRAFT status. */
    void submitJobRequisition(JobPosting jobPosting);

    /** Approves a DRAFT requisition so it can be published. */
    void approveJobRequisition(String jobId);

    /** Records a referral for an existing candidate. */
    void recordReferral(String candidateId, String referrerName);

    Application submitApplication(String applicationId, String candidateId, String jobId);

    ScreeningResult processCandidateApplication(String applicationId);
    
    void shortlistApplication(String applicationId);
    void rejectApplication(String applicationId);

    void scheduleInterview(InterviewSchedule schedule);
    
    /** Marks an interview as completed, moving the stage to INTERVIEW_COMPLETED. */
    void completeInterview(String applicationId);

    void recordOffer(Offer offer);

    void declineCandidate(String candidateId);

    EmployeeRecord hireCandidate(String candidateId, String employeeId,
                                 String department, String designation,
                                 LocalDate joiningDate);

    Collection<Application> getApplications();
    
    Collection<JobPosting> getJobPostings();
    
    Collection<Candidate> getCandidates();
    
    Collection<ScreeningResult> getScreeningResults();
    
    Collection<InterviewSchedule> getInterviewSchedules();
    
    Collection<Offer> getOffers();

    ApplicationStatus getStatus(String applicationId);

    ApplicationStatus getStatusByCandidateId(String candidateId);

    List<NotificationLog> getNotificationLogs();
    
    Collection<AuditEntry> getAuditEntries();

    void loadInitialData(Collection<JobPosting> jobs, Collection<Candidate> cands, 
                         Collection<Application> apps, Collection<ApplicationStatus> stats,
                         Collection<ScreeningResult> sResults, Collection<InterviewSchedule> schedules,
                         Collection<Offer> offerLetters, List<NotificationLog> logs);
}
