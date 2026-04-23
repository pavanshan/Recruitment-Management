package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.*;
import com.springbooters.recruitment.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * End-to-end pipeline test: Apply -> Screen -> Shortlist -> Interview -> Offer
 * Runs without a database or GUI to validate pure service logic.
 */
public class PipelineEndToEndTest {

    static int passed = 0;
    static int failed = 0;
    static RecruitmentFacade recruitment;
    static String appId;
    static String candidateId;
    static String jobId;

    public static void main(String[] args) {
        System.out.println("=== RECRUITMENT PIPELINE END-TO-END TEST ===\n");
        
        setup();
        
        testApply();
        testScreen();
        testShortlist();
        testInterviewNow();
        testGenerateOffer();
        testOfferStageInPipeline();
        
        System.out.println("\n========================================");
        System.out.println("RESULTS: " + passed + " passed, " + failed + " failed");
        System.out.println("========================================");
        
        if (failed > 0) System.exit(1);
    }

    static void setup() {
        ResumeAvailableRule resumeRule = new ResumeAvailableRule();
        resumeRule.linkWith(new ExperienceMatchRule()).linkWith(new SkillMatchRule());
        recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(resumeRule));
        
        // Create a job posting
        jobId = "JOB-TEST-001";
        JobPosting job = new JobPosting();
        job.setJobId(jobId);
        job.setTitle("Software Engineer");
        job.setDepartment("Engineering");
        job.setRequiredSkills("Java,Python");
        job.setExperience(2);
        job.setPostedDate(LocalDate.now());
        job.setStatus("ACTIVE");
        job.setPlatformName("LinkedIn");
        recruitment.submitJobRequisition(job);
        recruitment.approveJobRequisition(jobId);
        recruitment.publishJob(job);
        
        // Create a candidate
        candidateId = "CAN-TEST-001";
        Candidate c = new Candidate();
        c.setCandidateId(candidateId);
        c.setName("Alice Test");
        c.setContactInfo("alice@test.com");
        c.setResume("alice_resume.pdf");
        c.setSkills("Java,Python,Spring");
        c.setExperience(3);
        c.setSource("CAREER-PORTAL");
        recruitment.addCandidate(c);
        
        appId = "APP-TEST-001";
        System.out.println("Setup complete: Candidate=" + candidateId + ", Job=" + jobId + ", App=" + appId);
    }

    static void testApply() {
        System.out.println("\n--- STEP 1: Submit Application ---");
        try {
            Application app = recruitment.submitApplication(appId, candidateId, jobId);
            assertNotNull("Application created", app);
            
            ApplicationStatus status = recruitment.getStatus(appId);
            assertNotNull("Status created", status);
            assertEquals("Stage is APPLIED", "APPLIED", status.getCurrentStage());
            
            System.out.println("  Stage: " + status.getCurrentStage() + " [OK]");
        } catch (Exception e) {
            fail("Submit Application threw: " + e.getMessage());
        }
    }

    static void testScreen() {
        System.out.println("\n--- STEP 2: Screen Application ---");
        try {
            ScreeningResult result = recruitment.processCandidateApplication(appId);
            assertNotNull("Screening result created", result);
            
            ApplicationStatus status = recruitment.getStatus(appId);
            assertEquals("Stage is SCREENED", "SCREENED", status.getCurrentStage());
            
            System.out.println("  Stage: " + status.getCurrentStage() + " | Score: " + result.getScore() + " | Status: " + result.getShortlistStatus() + " [OK]");
        } catch (Exception e) {
            fail("Screen Application threw: " + e.getMessage());
        }
    }

    static void testShortlist() {
        System.out.println("\n--- STEP 3: Shortlist Application ---");
        try {
            recruitment.shortlistApplication(appId);
            
            ApplicationStatus status = recruitment.getStatus(appId);
            assertEquals("Stage is SHORTLISTED", "SHORTLISTED", status.getCurrentStage());
            
            System.out.println("  Stage: " + status.getCurrentStage() + " [OK]");
        } catch (Exception e) {
            fail("Shortlist threw: " + e.getMessage());
        }
    }

    static void testInterviewNow() {
        System.out.println("\n--- STEP 4: Interview Now (SHORTLISTED -> INTERVIEW_COMPLETED) ---");
        try {
            recruitment.completeInterview(appId);
            
            ApplicationStatus status = recruitment.getStatus(appId);
            assertEquals("Stage is INTERVIEW_COMPLETED", "INTERVIEW_COMPLETED", status.getCurrentStage());
            
            System.out.println("  Stage: " + status.getCurrentStage() + " [OK]");
        } catch (Exception e) {
            fail("Complete Interview threw: " + e.getMessage());
        }
    }

    static void testGenerateOffer() {
        System.out.println("\n--- STEP 5: Generate Offer (requires candidateId, NOT appId) ---");
        try {
            Offer offer = new OfferBuilder()
                    .withOfferId("OFF-TEST-001")
                    .forCandidate(candidateId)   // <-- MUST be candidateId
                    .withDetails("Full-time Software Engineer role, remote-friendly")
                    .withSalary(new BigDecimal("75000"))
                    .startingOn(LocalDate.now().plusDays(30))
                    .expiringOn(LocalDate.now().plusDays(37))
                    .withStatus(OfferStatus.PENDING)
                    .build();
            
            recruitment.recordOffer(offer);
            
            ApplicationStatus status = recruitment.getStatus(appId);
            assertEquals("Stage is OFFERED", "OFFERED", status.getCurrentStage());
            
            System.out.println("  Stage: " + status.getCurrentStage() + " | Salary: " + offer.getSalary() + " [OK]");
        } catch (Exception e) {
            fail("Generate Offer threw: " + e.getMessage());
        }
    }

    static void testOfferStageInPipeline() {
        System.out.println("\n--- STEP 6: Verify Offers Tab Pipeline Shows OFFERED Candidate ---");
        try {
            ApplicationStatus status = recruitment.getStatus(appId);
            String stage = status.getCurrentStage();
            boolean visibleInOffersTab = stage.equals("INTERVIEW_COMPLETED") || stage.equals("OFFERED");
            
            if (visibleInOffersTab) {
                System.out.println("  Candidate with stage '" + stage + "' would appear in Offers tab [OK]");
                passed++;
            } else {
                fail("Candidate in stage '" + stage + "' would NOT appear in Offers tab");
            }
        } catch (Exception e) {
            fail("Offers tab pipeline check threw: " + e.getMessage());
        }
    }
    
    // ---- Assertion helpers ----
    
    static void assertNotNull(String msg, Object obj) {
        if (obj != null) { System.out.println("  [PASS] " + msg); passed++; }
        else { System.out.println("  [FAIL] " + msg + " -> was null"); failed++; }
    }
    
    static void assertEquals(String msg, String expected, String actual) {
        if (expected.equals(actual)) { System.out.println("  [PASS] " + msg + " -> " + actual); passed++; }
        else { System.out.println("  [FAIL] " + msg + " | expected='" + expected + "' actual='" + actual + "'"); failed++; }
    }
    
    static void fail(String msg) {
        System.out.println("  [FAIL] " + msg);
        failed++;
    }
}
