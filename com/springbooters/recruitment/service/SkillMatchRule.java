package com.springbooters.recruitment.service;

import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.JobPosting;

/** Screening rule that checks whether resume skills match job description text. */
public class SkillMatchRule extends ScreeningRule {

    private static final int MAX_POINTS = 50;

    @Override
    protected void score(Candidate candidate, JobPosting jobPosting, ScreeningReport report) {
        if (candidate == null || jobPosting == null) {
            report.addScore(0, "Skills", MAX_POINTS);
            return;
        }

        String requiredStr = jobPosting.getRequiredSkills();
        if (requiredStr == null || requiredStr.trim().isEmpty()) {
            report.addScore(0, "Skills (No requirements defined)", MAX_POINTS);
            return;
        }

        String candidateContext = (candidate.getSkills() == null ? "" : candidate.getSkills()).toLowerCase();
        String[] requiredSkills = requiredStr.split(",");
        int requiredCount = 0;
        int matchedCount = 0;

        for (String skill : requiredSkills) {
            String trimmedSkill = skill.trim();
            if (trimmedSkill.isEmpty()) continue;
            requiredCount++;

            String regex = "(?i)(?<=^|[^a-zA-Z0-9])" + java.util.regex.Pattern.quote(trimmedSkill) + "(?=$|[^a-zA-Z0-9])";
            if (java.util.regex.Pattern.compile(regex).matcher(candidateContext).find()) {
                matchedCount++;
            }
        }

        int score = (requiredCount == 0) ? 0 : (int) ((matchedCount / (double) requiredCount) * MAX_POINTS);
        String stats = (requiredCount == 0) ? " (No Requirements)" : " (" + matchedCount + "/" + requiredCount + " matches)";
        report.addScore(score, "Skills" + stats, MAX_POINTS);
    }
}
