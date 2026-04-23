package com.springbooters.recruitment.service;

/**
 * Data holder for the screening pipeline.
 * Collects total points and the audit breakdown string.
 */
public class ScreeningReport {
    private int totalScore = 0;
    private final StringBuilder remarks = new StringBuilder();

    public void addScore(int points, String label, int maxPoints) {
        this.totalScore += points;
        this.remarks.append(String.format("[%s: %d/%d]", label, points, maxPoints));
    }

    public int getTotalScore() {
        return totalScore;
    }

    public String getRemarks() {
        return remarks.toString();
    }
}
