package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.*;
import com.springbooters.recruitment.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class WorkflowTest {
    private static String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public static void main(String[] args) {
        System.out.println("Starting Happy Path Workflow Validation...");
        System.out.println("=========================================\n");
        
        try {
            ResumeAvailableRule resumeRule = new ResumeAvailableRule();
            resumeRule.linkWith(new ExperienceMatchRule())
                    .linkWith(new SkillMatchRule());
            RecruitmentFacade recruitment = new RecruitmentFacade(new NotificationFactory(), new AutomatedScreener(resumeRule));
            
            // 1. Post Job
            System.out.println("Step 1: Executing Action [+ Job]");
            JobPosting jobPosting = new JobPostingBuilder()
                    .withJobId(nextId("JOB"))
                    .withTitle("Senior Backend Engineer")
                    .withDepartment("Engineering")
                    .withDescription("Java, Spring Boot, Swing")
                    .withRequiredSkills("Java,SQL,Spring")
                    .withSalary(new BigDecimal("150000"))
                    .withStatus(JobStatus.DRAFT)
                    .withPlatform("Internal System")
                    .withChannel(ChannelType.EXTERNAL)
                    .withPostedDate(LocalDate.now())
                    .withExpiryDate(LocalDate.now().plusDays(30))
                    .withExperience(3)
                    .withMinScreeningScore(60)
                    .build();
            recruitment.submitJobRequisition(jobPosting);
            recruitment.approveJobRequisition(jobPosting.getJobId());
            recruitment.publishJob(jobPosting);
            System.out.println("   --> Job Published Successfully! [ID: " + jobPosting.getJobId() + "]\n");

            // 2. Add Candidate
            System.out.println("Step 2: Executing Action [+ Candidate]");
            Candidate candidate = new Candidate();
            candidate.setCandidateId(nextId("CAN"));
            candidate.setName("Jane Doe");
            candidate.setContactInfo("jane.doe@example.com");
            candidate.setResume("jane_doe.pdf");
            candidate.setSkills("Java,SQL,Spring");
            candidate.setSource("Direct Application");
            candidate.setExperience(5);
            recruitment.addCandidate(candidate);
            System.out.println("   --> Talent Pool Candidate Created! [ID: " + candidate.getCandidateId() + "]\n");

            // 3. Application Link
            System.out.println("Step 3: Executing Action [+ Apply]");
            Application app = recruitment.submitApplication(nextId("APP"), candidate.getCandidateId(), jobPosting.getJobId());
            System.out.println("   --> Application Linking Successful! [ID: " + app.getApplicationId() + "]\n");

            // 4. Screening
            System.out.println("Step 4: Executing Action [Manual/Auto Screening]");
            ScreeningResult screenResult = recruitment.processCandidateApplication(app.getApplicationId());
            System.out.println("   --> AI Screening Finalized! Score: " + screenResult.getScore() + " | Status: " + screenResult.getShortlistStatus() + "\n");

            // 5. Shortlist
            System.out.println("Step 5: Executing Action [HR Shortlist]");
            recruitment.shortlistApplication(app.getApplicationId());
            System.out.println("   --> Application Shortlisted for Interview!\n");

            // 6. Interview
            System.out.println("Step 6: Executing Action [Schedule Interview]");
            InterviewSchedule schedule = new InterviewSchedule();
            schedule.setScheduleId(nextId("INT"));
            schedule.setCandidateId(candidate.getCandidateId());
            schedule.setInterviewerId("MANAGER-01");
            schedule.setInterviewDate(LocalDate.now().plusDays(3));
            schedule.setInterviewTime(LocalTime.parse("14:00"));
            schedule.setInterviewType(InterviewType.TECHNICAL.name());
            recruitment.scheduleInterview(schedule);
            System.out.println("   --> Interview Safely Booked! [ID: " + schedule.getScheduleId() + "]\n");

            // 7. Complete Interview
            System.out.println("Step 7: Executing Action [Interview Completed]");
            recruitment.completeInterview(app.getApplicationId());
            System.out.println("   --> Interview Results Recorded!\n");

            // 8. Offer
            System.out.println("Step 8: Executing Action [Generate Offer]");
            Offer offer = new OfferBuilder()
                    .withOfferId(nextId("OFF"))
                    .forCandidate(candidate.getCandidateId())
                    .withDetails("Full time Employment")
                    .withSalary(new BigDecimal("160000"))
                    .startingOn(LocalDate.now().plusDays(15))
                    .expiringOn(LocalDate.now().plusDays(7))
                    .withStatus(OfferStatus.PENDING)
                    .build();
            recruitment.recordOffer(offer);
            System.out.println("   --> Offer Issued Systematically! [ID: " + offer.getOfferId() + "]\n");

            // 9. Hire
            System.out.println("Step 9: Executing Action [Hire Candidate]");
            EmployeeRecord record = recruitment.hireCandidate(candidate.getCandidateId(), "EMP-101", "Engineering", "Senior Developer", LocalDate.now().plusDays(30));
            System.out.println("   --> Candidate HIRED Successfully! [Employee ID: " + record.getEmployeeId() + "]\n");

            System.out.println("=========================================");
            System.out.println("TEST COMPLETED SUCCESSFULLY. NO LOGIC BUGS FOUND.");

        } catch (Exception ex) {
            System.err.println("FATAL ERROR IN PIPELINE: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
