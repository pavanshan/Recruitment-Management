package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.*;
import com.springbooters.recruitment.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Specifically verifies that portal applications result in SCREENED status
 * (after auto-screening) and correctly create both candidate and application records.
 */
public class VerifyPortalFlow {
    public static void main(String[] args) {
        System.out.println("=== Verifying Hardened Portal Logic ===\n");
        
        try {
            RecruitmentFacade recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(new ResumeAvailableRule()));
            
            // 1. Setup an Active Job
            JobPosting job = new JobPosting();
            job.setJobId("JOB-PORTAL-01");
            job.setTitle("Senior Architect");
            job.setDepartment("Engineering");
            job.setStatus(JobStatus.DRAFT.name());
            job.setPlatformName("LinkedIn");
            
            recruitment.submitJobRequisition(job);
            recruitment.approveJobRequisition("JOB-PORTAL-01");
            recruitment.publishJob(job);
            
            System.out.println("[Step 1] Job is now ACTIVE and published.");

            // 2. Simulate Portal Application
            System.out.println("[Step 2] Simulating candidate self-registration via portal...");
            
            Candidate newCand = new Candidate();
            newCand.setCandidateId("CAN-PUB-001");
            newCand.setName("Sarah Jenkins");
            newCand.setContactInfo("sarah@example.com");
            newCand.setResume("/path/to/sarah_resume.pdf");
            newCand.setSkills("Java, Cloud, Architecture");
            newCand.setSource("CAREER-PORTAL");
            newCand.setExperience(10);
            
            recruitment.addCandidate(newCand);
            Application app = recruitment.submitApplication("APP-PUB-001", "CAN-PUB-001", "JOB-PORTAL-01");
            
            // New logic: Portal calls screening immediately
            recruitment.processCandidateApplication(app.getApplicationId());
            
            // 3. Verification
            System.out.println("[Step 3] Verifying records...");
            
            if (recruitment.getCandidates().stream().anyMatch(c -> c.getCandidateId().equals("CAN-PUB-001"))) {
                System.out.println("   --> Candidate record created successfully.");
            }
            
            ApplicationStatus status = recruitment.getStatus(app.getApplicationId());
            System.out.println("   --> Application Stage: " + status.getCurrentStage());
            
            // Must stay at SCREENED (per hardening instructions)
            if (ApplicationStage.SCREENED.name().equals(status.getCurrentStage())) {
                System.out.println("   --> SUCCESS: Application landed in SCREENED state (Manual Shortlist required).");
            } else {
                throw new RuntimeException("Unexpected initial stage: " + status.getCurrentStage());
            }

            System.out.println("\nPORTAL FLOW VERIFIED SUCCESSFULLY.");

        } catch (Exception ex) {
            System.err.println("\nVERIFICATION FAILED:");
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
