package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.JobPosting;

/**
 * Behavioral Pattern - Chain of Responsibility.
 *
 * Each rule owns one screening decision and passes control to the next rule
 * only when it succeeds.
 */
public abstract class ScreeningRule {

    private ScreeningRule nextRule;

    public ScreeningRule linkWith(ScreeningRule nextRule) {
        this.nextRule = nextRule;
        return nextRule;
    }

    public final void evaluate(Candidate candidate, JobPosting jobPosting, ScreeningReport report) {
        score(candidate, jobPosting, report);
        if (nextRule != null) {
            nextRule.evaluate(candidate, jobPosting, report);
        }
    }

    protected abstract void score(Candidate candidate, JobPosting jobPosting, ScreeningReport report);
}
