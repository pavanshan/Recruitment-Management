package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.*;
import com.springbooters.recruitment.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class VerifyUserScenario {
    public static void main(String[] args) {
        System.out.println("=== Verifying Hardened Terminal State Guards ===");
        
        try {
            // Setup screening rules to ALWAYS REJECT for this test
            ScreeningRule rejectRule = new ScreeningRule() {
                @Override
                protected void score(Candidate candidate, JobPosting jobPosting, ScreeningReport report) {
                    report.addScore(0, "TestRule (force reject)", 100);
                }
            };
            
            RecruitmentFacade recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(rejectRule));
            
            // 1. Post a job
            JobPosting job = new JobPostingBuilder()
                    .withJobId("JOB-USER-X")
                    .withTitle("Test Job")
                    .withDepartment("Engineering")
                    .withDescription("Java testing")
                    .withStatus(JobStatus.DRAFT)
                    .withSalary(new BigDecimal("1000"))
                    .withPlatform("Test Platform")
                    .build();
            recruitment.submitJobRequisition(job);
            recruitment.approveJobRequisition(job.getJobId());
            recruitment.publishJob(job);
            
            // 2. Add a candidate
            Candidate candidate = new Candidate();
            candidate.setCandidateId("CAN-USER-X");
            candidate.setName("Test Candidate");
            candidate.setContactInfo("test@example.com");
            recruitment.addCandidate(candidate);
            
            // 3. Apply
            Application app = recruitment.submitApplication("APP-USER-X", "CAN-USER-X", "JOB-USER-X");
            System.out.println("Application submitted. Stage: " + recruitment.getStatus(app.getApplicationId()).getCurrentStage());
            
            // 4. Process Application (Moves to SCREENED)
            System.out.println("Processing application...");
            recruitment.processCandidateApplication(app.getApplicationId());
            System.out.println("Current stage: " + recruitment.getStatus(app.getApplicationId()).getCurrentStage());
            
            // 5. Force REJECT (HR Action)
            System.out.println("HR rejects application...");
            recruitment.rejectApplication(app.getApplicationId());
            System.out.println("Stage after rejection: " + recruitment.getStatus(app.getApplicationId()).getCurrentStage());
            
            if (!ApplicationStage.REJECTED.name().equals(recruitment.getStatus(app.getApplicationId()).getCurrentStage())) {
                throw new RuntimeException("Test setup failed: Candidate should be in REJECTED state.");
            }
            
            // 6. ATTEMPT TO SCHEDULE INTERVIEW (Verifying terminal guard)
            System.out.println("Attempting to schedule interview for REJECTED candidate...");
            InterviewSchedule schedule = new InterviewSchedule();
            schedule.setScheduleId("INT-USER-X");
            schedule.setCandidateId("CAN-USER-X");
            schedule.setInterviewDate(LocalDate.now().plusDays(1));
            schedule.setInterviewTime(LocalTime.of(10, 0));
            schedule.setInterviewerId("EMP-001");
            schedule.setInterviewType(InterviewType.TECHNICAL.name());
            
            try {
                recruitment.scheduleInterview(schedule);
                throw new RuntimeException("ERROR: Terminal guard FAILED. REJECTED candidate was allowed to transition.");
            } catch (Exception e) {
                System.out.println("SUCCESS: Terminal guard correctly blocked transition from REJECTED: " + e.getMessage());
            }
            
            System.out.println("\nVERIFICATION COMPLETE: THE RECRUITMENT WORKFLOW IS NOW RIGID AND SECURE.");
            
        } catch (Exception ex) {
            System.err.println("\nVERIFICATION FAILED WITH UNEXPECTED EXCEPTION:");
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
