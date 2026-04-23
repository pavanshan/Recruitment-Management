package com.springbooters.recruitment.service;

import com.springbooters.recruitment.exception.RecruitmentException;
import com.springbooters.recruitment.model.Application;
import com.springbooters.recruitment.model.ApplicationStage;
import com.springbooters.recruitment.model.ApplicationStatus;
import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.EmployeeRecord;
import com.springbooters.recruitment.model.InterviewSchedule;
import com.springbooters.recruitment.model.JobPosting;
import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.NotificationType;
import com.springbooters.recruitment.model.Offer;
import com.springbooters.recruitment.model.OfferStatus;
import com.springbooters.recruitment.model.JobStatus;
import com.springbooters.recruitment.model.AuditEntry;
import com.springbooters.recruitment.model.ScreeningResult;
import com.springbooters.recruitment.db.RecruitmentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structural Pattern - Facade.
 *
 * Presents one simple workflow API for HR users while hiding candidate storage,
 * ATS status updates, screening, notifications and offer handling.
 *
 * GRASP - Controller: Receives subsystem operations.
 * SOLID - Single Responsibility: Coordinates recruitment workflow only; model
 * classes still own their own data validation.
 */
public class RecruitmentFacade implements RecruitmentIntegration {

    private final Map<String, Candidate> candidates = new HashMap<>();
    private final Map<String, JobPosting> jobPostings = new HashMap<>();
    private final Map<String, Application> applications = new HashMap<>();
    private final Map<String, ApplicationStatus> statuses = new HashMap<>();
    private final Map<String, Offer> offers = new HashMap<>();
    private final Map<String, ScreeningResult> screeningResults = new HashMap<>();
    private final Map<String, InterviewSchedule> interviewSchedules = new HashMap<>();
    private final List<NotificationLog> notificationLogs = new ArrayList<>();
    private final NotificationFactory notificationFactory;
    private final AutomatedScreener screener;
    private RecruitmentRepository repository;

    public RecruitmentFacade(NotificationFactory notificationFactory, AutomatedScreener screener) {
        this.notificationFactory = notificationFactory;
        this.screener = screener;
    }

    public void setRepository(RecruitmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addCandidate(Candidate candidate) {
        candidate.validate();
        candidates.put(candidate.getCandidateId(), candidate);
    }

    @Override
    public void publishJob(JobPosting jobPosting) {
        jobPosting.validate();
        JobPosting stored = jobPostings.get(jobPosting.getJobId());
        String currentStatus = (stored != null) ? stored.getStatus() : jobPosting.getStatus();

        if (!JobStatus.APPROVED.name().equals(currentStatus)) {
            throw new RecruitmentException("JOB_NOT_APPROVED",
                "Job must be APPROVED before publishing. Current status: " + currentStatus,
                RecruitmentException.ExceptionCategory.WARNING);
        }
        jobPosting.setStatus(JobStatus.ACTIVE.name());
        jobPostings.put(jobPosting.getJobId(), jobPosting);
        logAudit("PUBLISH_JOB", "Job published: " + jobPosting.getJobId() + " (" + jobPosting.getTitle() + ")");
    }

    @Override
    public void submitJobRequisition(JobPosting jobPosting) {
        jobPosting.validate();
        jobPosting.setStatus(JobStatus.DRAFT.name());
        jobPostings.put(jobPosting.getJobId(), jobPosting);
        logAudit("JOB_REQUISITION", "Requisition submitted: " + jobPosting.getJobId());
    }

    @Override
    public void approveJobRequisition(String jobId) {
        JobPosting job = requireJob(jobId);
        if (!JobStatus.DRAFT.name().equals(job.getStatus())) {
            throw new RecruitmentException("INVALID_STATUS",
                    "Only DRAFT requisitions can be approved. Current status: " + job.getStatus(),
                    RecruitmentException.ExceptionCategory.WARNING);
        }
        job.setStatus(JobStatus.APPROVED.name());
        logAudit("APPROVE_JOB", "Job approved: " + jobId);
    }

    @Override
    public void recordReferral(String candidateId, String referrerName) {
        Candidate candidate = requireCandidate(candidateId);
        candidate.setSource("REFERRAL");
        candidate.setReferredBy(referrerName);
        logAudit("RECORD_REFERRAL", "Referral recorded for " + candidateId + " by " + referrerName);
    }

    @Override
    public Application submitApplication(String applicationId, String candidateId, String jobId) {
        Candidate candidate = requireCandidate(candidateId);
        JobPosting jobPosting = requireJob(jobId);

        if (!jobPosting.isActive()) {
            throw new RecruitmentException("APPLICATION_CLOSED",
                    "Applications are closed for this job",
                    RecruitmentException.ExceptionCategory.WARNING);
        }

        // Check for duplicate application
        for (Application existing : applications.values()) {
            if (existing.getCandidateId().equals(candidateId) && existing.getJobId().equals(jobId)) {
                return existing;
            }
        }

        Application application = new Application(applicationId, candidateId, jobId, LocalDate.now());
        application.validate();
        applications.put(applicationId, application);

        ApplicationStatus status = new ApplicationStatus();
        status.setApplicationId(applicationId);
        status.updateStage(ApplicationStage.APPLIED.name());
        statuses.put(applicationId, status);

        sendNotification("EMAIL", NotificationType.APPLICATION_RECEIVED,
                candidate.getContactInfo(), "Your application has been received.");
        logAudit("SUBMIT_APPLICATION", "Candidate " + candidateId + " applied for job " + jobId + " [AppID: " + applicationId + "]");
        return application;
    }

    @Override
    public ScreeningResult processCandidateApplication(String applicationId) {
        return screenApplication(applicationId);
    }

    public ScreeningResult screenApplication(String applicationId) {
        Application application = requireApplication(applicationId);
        Candidate candidate = requireCandidate(application.getCandidateId());
        JobPosting jobPosting = requireJob(application.getJobId());

        ScreeningResult result = screener.screen(applicationId, candidate, jobPosting);
        
        ApplicationStatus status = statuses.get(applicationId);
        if (status != null && ApplicationStage.from(status.getCurrentStage()).isTerminal()) {
            logAudit("SCREENING_SKIPPED", candidate.getName() + ": Screening performed, but stage remains " + status.getCurrentStage() + " (Terminal State Guard)");
        } else {
            // Stage always becomes SCREENED; HR will shortlist/reject manually
            updateApplicationStage(applicationId, ApplicationStage.SCREENED);
        }
        
        screeningResults.put(applicationId, result);
        return result;
    }

    @Override
    public void shortlistApplication(String applicationId) {
        requireApplication(applicationId);
        updateApplicationStage(applicationId, ApplicationStage.SHORTLISTED);
        Candidate candidate = requireCandidate(requireApplication(applicationId).getCandidateId());
        logAudit("SHORTLIST", candidate.getName() + ": Application moved from SCREENED to SHORTLISTED [ID: " + applicationId + "]");
    }

    @Override
    public void rejectApplication(String applicationId) {
        requireApplication(applicationId);
        updateApplicationStage(applicationId, ApplicationStage.REJECTED);
        Candidate candidate = requireCandidate(requireApplication(applicationId).getCandidateId());
        logAudit("REJECT", candidate.getName() + ": Application REJECTED [ID: " + applicationId + "]");
    }

    @Override
    public void scheduleInterview(InterviewSchedule schedule) {
        schedule.validate();
        requireCandidate(schedule.getCandidateId());
        updateApplicationStageForCandidate(schedule.getCandidateId(), ApplicationStage.INTERVIEW_SCHEDULED);
        Candidate candidate = requireCandidate(schedule.getCandidateId());
        interviewSchedules.put(schedule.getScheduleId(), schedule);
        sendNotification("EMAIL", NotificationType.INTERVIEW_SCHEDULED,
                candidate.getContactInfo(), "Your interview has been scheduled.");
        logAudit("SCHEDULE_INTERVIEW", candidate.getName() + ": Interview scheduled on " + schedule.getInterviewDate() + " [Type: " + schedule.getInterviewType() + "]");
    }

    @Override
    public void completeInterview(String applicationId) {
        requireApplication(applicationId);
        updateApplicationStage(applicationId, ApplicationStage.INTERVIEW_COMPLETED);
        Candidate candidate = requireCandidate(requireApplication(applicationId).getCandidateId());
        logAudit("COMPLETE_INTERVIEW", candidate.getName() + ": Interview completed successfully.");
    }

    @Override
    public void recordOffer(Offer offer) {
        offer.validate();
        requireCandidate(offer.getCandidateId());
        offers.put(offer.getOfferId(), offer);
        updateApplicationStageForCandidate(offer.getCandidateId(), ApplicationStage.OFFERED);
        Candidate candidate = requireCandidate(offer.getCandidateId());
        sendNotification("EMAIL", NotificationType.OFFER_SENT,
                candidate.getContactInfo(), "Your offer letter has been generated.");
        String candidateName = requireCandidate(offer.getCandidateId()).getName();
        logAudit("RECORD_OFFER", candidateName + ": Job offer issued [Salary: " + offer.getSalary() + "]");
    }

    @Override
    public void declineCandidate(String candidateId) {
        requireCandidate(candidateId);
        updateApplicationStageForCandidate(candidateId, ApplicationStage.REJECTED);
        Candidate candidate = requireCandidate(candidateId);
        sendNotification("SMS", NotificationType.STATUS_UPDATE,
                candidate.getContactInfo(), "Thank you for your time. Your application for this position will not be moving forward at this time.");
        logAudit("DECLINE_CANDIDATE", "Candidate declined: " + candidateId);
    }

    @Override
    public EmployeeRecord hireCandidate(String candidateId, String employeeId,
                                        String department, String designation,
                                        LocalDate joiningDate) {
        Candidate candidate = requireCandidate(candidateId);
        Offer offer = requireOfferForCandidate(candidateId);

        if (offer.isExpired()) {
            throw new RecruitmentException("OFFER_EXPIRED",
                    "Expired offers cannot be converted into employee records",
                    RecruitmentException.ExceptionCategory.WARNING);
        }
        if (!OfferStatus.ACCEPTED.name().equals(offer.getStatus())) {
            offer.accept();
        }

        updateApplicationStageForCandidate(candidateId, ApplicationStage.HIRED);

        EmployeeRecord employeeRecord = new EmployeeRecord(
                employeeId,
                candidateId,
                candidate.getName(),
                department,
                designation,
                joiningDate);
        employeeRecord.validate();

        sendNotification("EMAIL", NotificationType.STATUS_UPDATE,
                candidate.getContactInfo(), "Congratulations, you have been hired.");
        logAudit("HIRE_CANDIDATE", candidate.getName() + ": Candidate HIRED as " + designation + " in " + department);
        return employeeRecord;
    }

    @Override
    public ApplicationStatus getStatusByCandidateId(String candidateId) {
        for (Application application : applications.values()) {
            if (candidateId.equals(application.getCandidateId())) {
                return statuses.get(application.getApplicationId());
            }
        }
        throw new RecruitmentException("APPLICATION_NOT_FOUND",
                "No application exists for candidate: " + candidateId,
                RecruitmentException.ExceptionCategory.MINOR);
    }

    @Override
    public Collection<Application> getApplications() {
        return applications.values();
    }
    
    @Override
    public Collection<JobPosting> getJobPostings() {
        return jobPostings.values();
    }
    
    @Override
    public Collection<Candidate> getCandidates() {
        return candidates.values();
    }
    
    @Override
    public Collection<ScreeningResult> getScreeningResults() {
        return screeningResults.values();
    }
    
    @Override
    public Collection<InterviewSchedule> getInterviewSchedules() {
        return interviewSchedules.values();
    }
    
    @Override
    public Collection<Offer> getOffers() {
        return offers.values();
    }

    @Override
    public ApplicationStatus getStatus(String applicationId) {
        return statuses.get(applicationId);
    }

    @Override
    public List<NotificationLog> getNotificationLogs() {
        return Collections.unmodifiableList(notificationLogs);
    }

    @Override
    public Collection<AuditEntry> getAuditEntries() {
        if (repository != null) {
            try {
                return repository.getAllAuditEntries();
            } catch (Exception e) {
                System.err.println("Failed to fetch audit entries: " + e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    private void logAudit(String action, String details) {
        AuditEntry entry = new AuditEntry(action, "Admin", details);
        if (repository != null) {
            try {
                repository.saveAuditEntry(entry);
            } catch (Exception e) {
                System.err.println("Failed to persist audit log: " + e.getMessage());
            }
        }
    }

    private NotificationLog sendNotification(String channel, NotificationType type,
                                             String contactInfo, String message) {
        NotificationSender sender = notificationFactory.createSender(channel);
        NotificationLog log = sender.send(type, contactInfo, message);
        notificationLogs.add(log);
        return log;
    }

    private Candidate requireCandidate(String candidateId) {
        Candidate candidate = candidates.get(candidateId);
        if (candidate == null) {
            throw new RecruitmentException("CANDIDATE_NOT_FOUND",
                    "Candidate does not exist: " + candidateId,
                    RecruitmentException.ExceptionCategory.MINOR);
        }
        return candidate;
    }

    private JobPosting requireJob(String jobId) {
        JobPosting jobPosting = jobPostings.get(jobId);
        if (jobPosting == null) {
            throw new RecruitmentException("JOB_NOT_FOUND",
                    "Job posting does not exist: " + jobId,
                    RecruitmentException.ExceptionCategory.MINOR);
        }
        return jobPosting;
    }

    private Application requireApplication(String applicationId) {
        Application application = applications.get(applicationId);
        if (application == null) {
            throw new RecruitmentException("APPLICATION_NOT_FOUND",
                    "Application does not exist: " + applicationId,
                    RecruitmentException.ExceptionCategory.MINOR);
        }
        return application;
    }

    private Offer requireOfferForCandidate(String candidateId) {
        for (Offer offer : offers.values()) {
            if (candidateId.equals(offer.getCandidateId())) {
                return offer;
            }
        }
        throw new RecruitmentException("OFFER_NOT_FOUND",
                "No offer exists for candidate: " + candidateId,
                RecruitmentException.ExceptionCategory.MINOR);
    }

    private void updateApplicationStage(String applicationId, ApplicationStage stage) {
        ApplicationStatus status = statuses.get(applicationId);
        if (status == null) {
            throw new RecruitmentException("STATUS_NOT_FOUND",
                    "Application status does not exist: " + applicationId,
                    RecruitmentException.ExceptionCategory.MINOR);
        }
        try {
            status.updateStage(stage.name());
        } catch (IllegalStateException ex) {
            throw new RecruitmentException("INVALID_STAGE_TRANSITION",
                    ex.getMessage(),
                    RecruitmentException.ExceptionCategory.WARNING, ex);
        }
    }

    private void updateApplicationStageForCandidate(String candidateId, ApplicationStage stage) {
        for (Application application : applications.values()) {
            if (candidateId.equals(application.getCandidateId())) {
                updateApplicationStage(application.getApplicationId(), stage);
                return;
            }
        }
        throw new RecruitmentException("APPLICATION_NOT_FOUND",
                "No application exists for candidate: " + candidateId,
                RecruitmentException.ExceptionCategory.MINOR);
    }

    public String generateOfferLetterHtml(String offerId) {
        Offer offer = offers.get(offerId);
        if (offer == null) {
            throw new RecruitmentException("OFFER_NOT_FOUND", "Offer not found: " + offerId, RecruitmentException.ExceptionCategory.MINOR);
        }
        Candidate candidate = requireCandidate(offer.getCandidateId());
        
        return "<html><body style='font-family: Arial, sans-serif; padding: 40px;'>" +
               "<div style='text-align: center;'><h1 style='color: #2c3e50;'>OFFER OF EMPLOYMENT</h1></div>" +
               "<hr/><br/>" +
               "<p><b>Date:</b> " + LocalDate.now() + "</p>" +
               "<p><b>To:</b> " + candidate.getName() + "</p>" +
               "<p><b>Address:</b> " + candidate.getContactInfo() + "</p>" +
               "<br/><p>Dear " + candidate.getName() + ",</p>" +
               "<p>We are pleased to offer you the position at our organization. Based on your skills and experience, we are confident you will be a valuable addition to our team.</p>" +
               "<h3>Terms and Conditions:</h3>" +
               "<ul>" +
               "<li><b>Role Details:</b> " + offer.getOfferDetails() + "</li>" +
               "<li><b>Annual Compensation:</b> $" + offer.getSalary() + "</li>" +
               "<li><b>Joining Date:</b> " + offer.getStartDate() + "</li>" +
               "<li><b>Offer Expiry:</b> " + offer.getExpiryDate() + "</li>" +
               "</ul>" +
               "<p>Please sign and return a copy of this letter to confirm your acceptance.</p>" +
               "<br/><br/><p>Best Regards,</p>" +
               "<p><b>Human Resources Team</b><br/>HRMS Global</p>" +
               "</body></html>";
    }

    @Override
    public void loadInitialData(Collection<JobPosting> jobs, Collection<Candidate> cands,
                                 Collection<Application> apps, Collection<ApplicationStatus> stats,
                                 Collection<ScreeningResult> sResults, Collection<InterviewSchedule> schedules,
                                 Collection<Offer> offerLetters, List<NotificationLog> logs) {
        jobPostings.clear();
        jobs.forEach(j -> jobPostings.put(j.getJobId(), j));
        
        candidates.clear();
        cands.forEach(c -> candidates.put(c.getCandidateId(), c));
        
        applications.clear();
        apps.forEach(a -> applications.put(a.getApplicationId(), a));
        
        statuses.clear();
        stats.forEach(s -> statuses.put(s.getApplicationId(), s));
        
        screeningResults.clear();
        sResults.forEach(sr -> screeningResults.put(sr.getApplicationId(), sr));
        
        interviewSchedules.clear();
        schedules.forEach(is -> interviewSchedules.put(is.getScheduleId(), is));
        
        offers.clear();
        offerLetters.forEach(o -> offers.put(o.getOfferId(), o));
        
        notificationLogs.clear();
        notificationLogs.addAll(logs);
    }
}
