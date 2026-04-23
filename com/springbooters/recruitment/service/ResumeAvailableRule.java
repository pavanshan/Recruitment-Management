package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.JobPosting;

/** Screening rule that rejects candidates without a resume. */
public class ResumeAvailableRule extends ScreeningRule {

    @Override
    protected void score(Candidate candidate, JobPosting jobPosting, ScreeningReport report) {
        int score = (candidate != null && candidate.hasResume()) ? 10 : 0;
        report.addScore(score, "Resume", 10);
    }
}
