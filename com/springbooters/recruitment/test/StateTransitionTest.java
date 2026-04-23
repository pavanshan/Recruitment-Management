package com.springbooters.recruitment.test;

import com.springbooters.recruitment.model.ApplicationStage;
import com.springbooters.recruitment.model.ApplicationStatus;

public class StateTransitionTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Hardened State Transition Verification ===");
        
        try {
            // Test 1: APPLIED -> SCREENED (Standard)
            testTransition(null, "APPLIED", true);
            testTransition("APPLIED", "SCREENED", true);
            
            // Test 2: SCREENED -> SHORTLISTED (Mandatory HR Step)
            testTransition("SCREENED", "SHORTLISTED", true);
            
            // Test 3: SHORTLISTED -> INTERVIEW_SCHEDULED (Standard)
            testTransition("SHORTLISTED", "INTERVIEW_SCHEDULED", true);

            // Test 4: INTERVIEW_SCHEDULED -> INTERVIEW_COMPLETED (Hardened Step)
            testTransition("INTERVIEW_SCHEDULED", "INTERVIEW_COMPLETED", true);
            
            // Test 5: INTERVIEW_COMPLETED -> OFFERED (Standard)
            testTransition("INTERVIEW_COMPLETED", "OFFERED", true);
            
            // Test 6: REJECTED -> Terminal (Should FAIL now)
            testTransition("REJECTED", "INTERVIEW_SCHEDULED", false);
            
            // Test 7: HIRED -> Terminal (Should FAIL)
            testTransition("HIRED", "OFFERED", false);
            
            // Test 8: Skip Steps (Should FAIL)
            testTransition("SCREENED", "OFFERED", false);

            System.out.println("\nALL HARDENED TRANSITION TESTS PASSED SUCCESSFULLY!");
        } catch (Exception e) {
            System.err.println("\nTEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testTransition(String from, String to, boolean expectedSuccess) {
        ApplicationStatus status = new ApplicationStatus();
        if (from != null) {
            status.updateStage(from);
        }
        
        System.out.print("Testing transition: [" + (from == null ? "START" : from) + "] -> [" + to + "] ... ");
        
        try {
            status.updateStage(to);
            if (expectedSuccess) {
                System.out.println("PASS (Success)");
            } else {
                throw new RuntimeException("FAIL (Expected IllegalStateException but transition succeeded)");
            }
        } catch (IllegalStateException e) {
            if (!expectedSuccess) {
                System.out.println("PASS (Caught expected terminal guard: " + e.getMessage() + ")");
            } else {
                throw new RuntimeException("FAIL (Expected success but got exception: " + e.getMessage() + ")");
            }
        }
    }
}
