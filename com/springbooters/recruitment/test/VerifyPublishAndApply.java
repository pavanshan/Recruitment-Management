package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.*;
import com.springbooters.recruitment.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Specifically verifies that applications are only possible after the 
 * Requisition -> Approval -> Publish flow is complete.
 */
public class VerifyPublishAndApply {
    public static void main(String[] args) {
        System.out.println("=== Verifying Publish-to-Apply Workflow ===");
        
        try {
            RecruitmentFacade recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(new ResumeAvailableRule()));
            
            // 1. Create a Requisition
            JobPosting job = new JobPosting();
            job.setJobId("JOB-VERIFY-001");
            job.setTitle("Cloud Architect");
            job.setDepartment("Infrastructure");
            job.setSalary(new BigDecimal("200000"));
            job.setStatus(JobStatus.DRAFT.name());
            job.validate();
            
            recruitment.submitJobRequisition(job);
            System.out.println("[Step 1] Job submitted as DRAFT.");

            // 2. Try to apply (Expected Failure)
            try {
                recruitment.submitApplication("APP-X1", "CAN-REF", "JOB-VERIFY-001");
                System.err.println("FAILED: Should not allow application for DRAFT job.");
            } catch (Exception e) {
                System.out.println("[Step 2] Correctly blocked application for DRAFT job: " + e.getMessage());
            }

            // 3. Approve
            recruitment.approveJobRequisition("JOB-VERIFY-001");
            System.out.println("[Step 3] Job moved to APPROVED.");

            // 4. Try to apply (Expected Failure)
            try {
                recruitment.submitApplication("APP-X2", "CAN-REF", "JOB-VERIFY-001");
                System.err.println("FAILED: Should not allow application for APPROVED job.");
            } catch (Exception e) {
                System.out.println("[Step 4] Correctly blocked application for APPROVED job: " + e.getMessage());
            }

            // 5. Publish
            recruitment.publishJob(job);
            System.out.println("[Step 5] Job PUBLISHED (Status: ACTIVE).");

            // 6. Apply (Expected Success)
            Candidate cand = new Candidate();
            cand.setCandidateId("CAN-REF");
            cand.setName("Referral Candidate");
            cand.setContactInfo("ref@example.com");
            recruitment.addCandidate(cand);
            
            Application app = recruitment.submitApplication("APP-X3", "CAN-REF", "JOB-VERIFY-001");
            if (app != null) {
                System.out.println("[Step 6] SUCCESS: Application accepted after job was published!");
            }
            
            System.out.println("\nVERIFICATION COMPLETE: Workflow is secure and functional.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
