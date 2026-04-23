package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.JobPosting;

/**
 * Phase 9 Experience Matching Rule.
 * Tiered scoring:
 * - Match/Exceed: 40 pts
 * - Within 1 year: 30 pts
 * - Within 2 years: 15 pts
 * - Else: 0 pts
 */
public class ExperienceMatchRule extends ScreeningRule {

    private static final int MAX_POINTS = 40;

    @Override
    protected void score(Candidate candidate, JobPosting jobPosting, ScreeningReport report) {
        int candidateExp = candidate.getExperience();
        int requiredExp = jobPosting.getExperience();
        
        int score = 0;
        String tier = " (Insufficient)";
        int diff = requiredExp - candidateExp;

        if (candidateExp == 0 && requiredExp > 0) {
            // Fresher tier: awards partial credit for potential
            score = 20;
            tier = " (Fresher – Potential)";
        } else if (diff <= 0) {
            score = 40;
            tier = " (Exact Match)";
        } else if (diff <= 1) {
            score = 30;
            tier = " (1yr Deficit)";
        } else if (diff <= 2) {
            score = 15;
            tier = " (2yr Deficit)";
        }

        report.addScore(score, "Exp" + tier, MAX_POINTS);
    }
}
