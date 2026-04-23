package com.springbooters.recruitment.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a scheduled interview session between a candidate and an interviewer.
 *
 * GRASP - Creator: InterviewService creates InterviewSchedule since
 * it aggregates all required data (candidateId, interviewerId, slot info).
 */
public class InterviewSchedule {

    private String scheduleId;
    private String candidateId;
    private String interviewerId;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String interviewType;   // PHONE, VIDEO, IN_PERSON, TECHNICAL

    public InterviewSchedule() {}

    /**
     * Basic validation to ensure schedule consistency.
     */
    public void validate() {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            throw new IllegalStateException("Candidate ID is required");
        }

        if (interviewerId == null || interviewerId.trim().isEmpty()) {
            throw new IllegalStateException("Interviewer ID is required");
        }

        if (interviewDate == null || interviewTime == null) {
            throw new IllegalStateException("Interview date and time are required");
        }

        LocalDateTime scheduledAt = LocalDateTime.of(interviewDate, interviewTime);
        if (!scheduledAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                "Interview must be scheduled in the future. Provided: "
                + interviewDate + " " + interviewTime);
        }

        InterviewType.from(interviewType);
    }

    public String getScheduleId()                       { return scheduleId; }
    public void setScheduleId(String scheduleId)        { this.scheduleId = scheduleId; }

    public String getCandidateId()                      { return candidateId; }
    public void setCandidateId(String candidateId)      { this.candidateId = candidateId; }

    public String getInterviewerId()                    { return interviewerId; }
    public void setInterviewerId(String interviewerId)  { this.interviewerId = interviewerId; }

    public LocalDate getInterviewDate()                 { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate){ this.interviewDate = interviewDate; }

    public LocalTime getInterviewTime()                 { return interviewTime; }
    public void setInterviewTime(LocalTime interviewTime){ this.interviewTime = interviewTime; }

    public String getInterviewType()                    { return interviewType; }

    public void setInterviewType(String interviewType) {
        this.interviewType = InterviewType.from(interviewType).name();
    }

    @Override
    public String toString() {
        return "InterviewSchedule{scheduleId='" + scheduleId
                + "', candidateId='" + candidateId
                + "', interviewerId='" + interviewerId
                + "', date=" + interviewDate
                + ", time=" + interviewTime + "}";
    }
}
