package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.JobPosting;
import com.springbooters.recruitment.model.ScreeningResult;
import com.springbooters.recruitment.model.ShortlistStatus;

/**
 * GRASP - Controller: Coordinates screening rules and creates the result
 * object used by the ATS workflow.
 */
public class AutomatedScreener {

    private final ScreeningRule firstRule;

    public AutomatedScreener(ScreeningRule firstRule) {
        this.firstRule = firstRule;
    }

    public ScreeningResult screen(String applicationId, Candidate candidate, JobPosting jobPosting) {
        ScreeningReport report = new ScreeningReport();
        if (firstRule != null) {
            firstRule.evaluate(candidate, jobPosting, report);
        }

        int score = report.getTotalScore();
        // Threshold check: Status is SHORTLISTED only if totalScore >= minScreeningScore
        boolean passes = score >= jobPosting.getMinScreeningScore();

        ScreeningResult result = new ScreeningResult();
        result.setApplicationId(applicationId);
        result.setScore(score);
        result.setRanking(passes ? 1 : 99);
        result.setShortlistStatus(passes ? ShortlistStatus.SHORTLISTED.name() : ShortlistStatus.REJECTED.name());
        result.setRemarks(report.getRemarks());
        result.validate();
        return result;
    }
}
