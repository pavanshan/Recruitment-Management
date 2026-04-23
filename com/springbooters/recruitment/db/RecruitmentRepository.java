package com.springbooters.recruitment.db;

import com.springbooters.recruitment.model.Application;
import com.springbooters.recruitment.model.ApplicationStatus;
import com.springbooters.recruitment.model.AuditEntry;
import com.springbooters.recruitment.model.Candidate;
import com.springbooters.recruitment.model.InterviewSchedule;
import com.springbooters.recruitment.model.JobPosting;
import com.springbooters.recruitment.model.NotificationLog;
import com.springbooters.recruitment.model.Offer;
import com.springbooters.recruitment.model.ScreeningResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for storing recruitment workflow data in MySQL.
 *
 * GRASP - Pure Fabrication: database persistence is separated from domain
 * models so entity classes remain focused on recruitment data and validation.
 */
public class RecruitmentRepository {

    private final DatabaseConnection databaseConnection;

    public RecruitmentRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void saveCandidate(Candidate candidate) throws SQLException {
        candidate.validate();
        String sql = "INSERT INTO candidates "
                + "(candidate_id, name, contact_info, resume, skills, source, experience, referred_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(candidate_id) DO UPDATE SET name=?, contact_info=?, resume=?, skills=?, source=?, experience=?, referred_by=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, candidate.getCandidateId());
            statement.setString(2, candidate.getName());
            statement.setString(3, candidate.getContactInfo());
            statement.setString(4, candidate.getResume());
            statement.setString(5, candidate.getSkills());
            statement.setString(6, candidate.getSource());
            statement.setInt(7, candidate.getExperience());
            statement.setString(8, candidate.getReferredBy());
            
            statement.setString(9, candidate.getName());
            statement.setString(10, candidate.getContactInfo());
            statement.setString(11, candidate.getResume());
            statement.setString(12, candidate.getSkills());
            statement.setString(13, candidate.getSource());
            statement.setInt(14, candidate.getExperience());
            statement.setString(15, candidate.getReferredBy());
            statement.executeUpdate();
        }
    }

    public void saveJobPosting(JobPosting jobPosting) throws SQLException {
        jobPosting.validate();
        String sql = "INSERT INTO job_postings "
                + "(job_id, title, department, description, salary, status, platform_name, channel_type, posted_date, expiry_date, experience, min_screening_score, required_skills) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(job_id) DO UPDATE SET title=?, department=?, description=?, salary=?, status=?, platform_name=?, channel_type=?, posted_date=?, expiry_date=?, experience=?, min_screening_score=?, required_skills=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, jobPosting.getJobId());
            statement.setString(2, jobPosting.getTitle());
            statement.setString(3, jobPosting.getDepartment());
            statement.setString(4, jobPosting.getDescription());
            statement.setBigDecimal(5, jobPosting.getSalary());
            statement.setString(6, jobPosting.getStatus());
            statement.setString(7, jobPosting.getPlatformName());
            statement.setString(8, jobPosting.getChannelType());
            statement.setDate(9, jobPosting.getPostedDate() != null ? Date.valueOf(jobPosting.getPostedDate()) : null);
            statement.setDate(10, jobPosting.getExpiryDate() != null ? Date.valueOf(jobPosting.getExpiryDate()) : null);
            statement.setInt(11, jobPosting.getExperience());
            statement.setInt(12, jobPosting.getMinScreeningScore());
            statement.setString(13, jobPosting.getRequiredSkills());

            statement.setString(14, jobPosting.getTitle());
            statement.setString(15, jobPosting.getDepartment());
            statement.setString(16, jobPosting.getDescription());
            statement.setBigDecimal(17, jobPosting.getSalary());
            statement.setString(18, jobPosting.getStatus());
            statement.setString(19, jobPosting.getPlatformName());
            statement.setString(20, jobPosting.getChannelType());
            statement.setDate(21, jobPosting.getPostedDate() != null ? Date.valueOf(jobPosting.getPostedDate()) : null);
            statement.setDate(22, jobPosting.getExpiryDate() != null ? Date.valueOf(jobPosting.getExpiryDate()) : null);
            statement.setInt(23, jobPosting.getExperience());
            statement.setInt(24, jobPosting.getMinScreeningScore());
            statement.setString(25, jobPosting.getRequiredSkills());
            statement.executeUpdate();
        }
    }

    public void saveApplication(Application application) throws SQLException {
        application.validate();
        String sql = "INSERT INTO applications (application_id, candidate_id, job_id, date_applied) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT(application_id) DO UPDATE SET candidate_id=?, job_id=?, date_applied=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, application.getApplicationId());
            statement.setString(2, application.getCandidateId());
            statement.setString(3, application.getJobId());
            statement.setDate(4, Date.valueOf(application.getDateApplied()));
            statement.setString(5, application.getCandidateId());
            statement.setString(6, application.getJobId());
            statement.setDate(7, Date.valueOf(application.getDateApplied()));
            statement.executeUpdate();
        }
    }

    public void saveApplicationStatus(ApplicationStatus status) throws SQLException {
        String sql = "INSERT INTO application_statuses (application_id, current_stage, history, updated_at) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT(application_id) DO UPDATE SET current_stage=?, history=?, updated_at=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp timestamp = Timestamp.valueOf(status.getTimestamp());
            statement.setString(1, status.getApplicationId());
            statement.setString(2, status.getCurrentStage());
            statement.setString(3, status.getHistory());
            statement.setTimestamp(4, timestamp);
            statement.setString(5, status.getCurrentStage());
            statement.setString(6, status.getHistory());
            statement.setTimestamp(7, timestamp);
            statement.executeUpdate();
        }
    }

    public void saveScreeningResult(ScreeningResult result) throws SQLException {
        result.validate();
        String sql = "INSERT INTO screening_results (application_id, score, ranking, shortlist_status, remarks) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT(application_id) DO UPDATE SET score=?, ranking=?, shortlist_status=?, remarks=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, result.getApplicationId());
            statement.setInt(2, result.getScore());
            statement.setInt(3, result.getRanking());
            statement.setString(4, result.getShortlistStatus());
            statement.setString(5, result.getRemarks());
            statement.setInt(6, result.getScore());
            statement.setInt(7, result.getRanking());
            statement.setString(8, result.getShortlistStatus());
            statement.setString(9, result.getRemarks());
            statement.executeUpdate();
        }
    }

    public void saveInterviewSchedule(InterviewSchedule schedule) throws SQLException {
        schedule.validate();
        String sql = "INSERT INTO interview_schedules "
                + "(schedule_id, candidate_id, interviewer_id, interview_date, interview_time, interview_type) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(schedule_id) DO UPDATE SET candidate_id=?, interviewer_id=?, interview_date=?, interview_time=?, interview_type=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schedule.getScheduleId());
            statement.setString(2, schedule.getCandidateId());
            statement.setString(3, schedule.getInterviewerId());
            statement.setDate(4, Date.valueOf(schedule.getInterviewDate()));
            statement.setTime(5, Time.valueOf(schedule.getInterviewTime()));
            statement.setString(6, schedule.getInterviewType());
            statement.setString(7, schedule.getCandidateId());
            statement.setString(8, schedule.getInterviewerId());
            statement.setDate(9, Date.valueOf(schedule.getInterviewDate()));
            statement.setTime(10, Time.valueOf(schedule.getInterviewTime()));
            statement.setString(11, schedule.getInterviewType());
            statement.executeUpdate();
        }
    }

    public void saveOffer(Offer offer) throws SQLException {
        offer.validate();
        String sql = "INSERT INTO offers "
                + "(offer_id, candidate_id, offer_details, salary, start_date, status, expiry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(offer_id) DO UPDATE SET candidate_id=?, offer_details=?, salary=?, start_date=?, status=?, expiry_date=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, offer.getOfferId());
            statement.setString(2, offer.getCandidateId());
            statement.setString(3, offer.getOfferDetails());
            statement.setBigDecimal(4, offer.getSalary());
            statement.setDate(5, Date.valueOf(offer.getStartDate()));
            statement.setString(6, offer.getStatus());
            statement.setDate(7, Date.valueOf(offer.getExpiryDate()));
            statement.setString(8, offer.getCandidateId());
            statement.setString(9, offer.getOfferDetails());
            statement.setBigDecimal(10, offer.getSalary());
            statement.setDate(11, Date.valueOf(offer.getStartDate()));
            statement.setString(12, offer.getStatus());
            statement.setDate(13, Date.valueOf(offer.getExpiryDate()));
            statement.executeUpdate();
        }
    }

    public void saveNotificationLog(NotificationLog log) throws SQLException {
        log.validate();
        String sql = "INSERT INTO notification_logs "
                + "(notification_id, notification_type, sent_ads, status_alert, contact_info_used, sent_timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(notification_id) DO UPDATE SET notification_type=?, sent_ads=?, status_alert=?, "
                + "contact_info_used=?, sent_timestamp=?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, log.getNotificationId());
            statement.setString(2, log.getNotificationType());
            statement.setString(3, log.getSentAds());
            statement.setString(4, log.getStatusAlert());
            statement.setString(5, log.getContactInfoUsed());
            statement.setTimestamp(6, Timestamp.valueOf(log.getSentTimestamp()));
            
            statement.setString(7, log.getNotificationType());
            statement.setString(8, log.getSentAds());
            statement.setString(9, log.getStatusAlert());
            statement.setString(10, log.getContactInfoUsed());
            statement.setTimestamp(11, Timestamp.valueOf(log.getSentTimestamp()));
            statement.executeUpdate();
        }
    }

    public List<JobPosting> getAllJobPostings() throws SQLException {
        List<JobPosting> jobs = new ArrayList<>();
        String sql = "SELECT * FROM job_postings";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                JobPosting job = new JobPosting();
                job.setJobId(rs.getString("job_id"));
                job.setTitle(rs.getString("title"));
                job.setDepartment(rs.getString("department"));
                job.setDescription(rs.getString("description"));
                job.setSalary(rs.getBigDecimal("salary"));
                job.setStatus(rs.getString("status"));
                job.setPlatformName(rs.getString("platform_name"));
                job.setChannelType(rs.getString("channel_type"));
                Date pDate = rs.getDate("posted_date");
                if (pDate != null) job.setPostedDate(pDate.toLocalDate());
                Date eDate = rs.getDate("expiry_date");
                if (eDate != null) job.setExpiryDate(eDate.toLocalDate());
                job.setExperience(rs.getInt("experience"));
                job.setMinScreeningScore(safeGetInt(rs, "min_screening_score", 60));
                job.setRequiredSkills(rs.getString("required_skills"));
                jobs.add(job);
            }
        }
        return jobs;
    }

    public List<Candidate> getAllCandidates() throws SQLException {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Candidate c = new Candidate();
                c.setCandidateId(rs.getString("candidate_id"));
                c.setName(rs.getString("name"));
                c.setContactInfo(rs.getString("contact_info"));
                c.setResume(rs.getString("resume"));
                c.setSkills(rs.getString("skills"));
                c.setSource(rs.getString("source"));
                c.setExperience(rs.getInt("experience"));
                c.setReferredBy(rs.getString("referred_by"));
                candidates.add(c);
            }
        }
        return candidates;
    }

    public List<Application> getAllApplications() throws SQLException {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT * FROM applications";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Application a = new Application();
                a.setApplicationId(rs.getString("application_id"));
                a.setCandidateId(rs.getString("candidate_id"));
                a.setJobId(rs.getString("job_id"));
                Date dApp = rs.getDate("date_applied");
                if (dApp != null) a.setDateApplied(dApp.toLocalDate());
                apps.add(a);
            }
        }
        return apps;
    }

    public List<ApplicationStatus> getAllApplicationStatuses() throws SQLException {
        List<ApplicationStatus> statuses = new ArrayList<>();
        String sql = "SELECT * FROM application_statuses";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ApplicationStatus s = new ApplicationStatus();
                s.setApplicationId(rs.getString("application_id"));
                // status requires updateStage to be set properly because it handles history updates internally
                // but for loading, we might need a way to set currentStage directly if we don't want history bloat
                // However, ApplicationStatus doesn't have setCurrentStage. 
                // We'll use reflection of the stage through history or just handle it as a fresh status for now if needed.
                // Re-checking ApplicationStatus.java... it has updateStage which bloats history.
                // We'll assume the currentStage in DB is the latest.
                String dbStage = rs.getString("current_stage");
                s.setHistory(rs.getString("history"));
                Timestamp ts = rs.getTimestamp("updated_at");
                if (ts != null) s.setTimestamp(ts.toLocalDateTime());
                
                if (dbStage != null) {
                    s.setCurrentStage(dbStage);
                }
                statuses.add(s);
            }
        }
        return statuses;
    }

    public List<ScreeningResult> getAllScreeningResults() throws SQLException {
        List<ScreeningResult> results = new ArrayList<>();
        String sql = "SELECT * FROM screening_results";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ScreeningResult sr = new ScreeningResult();
                sr.setApplicationId(rs.getString("application_id"));
                sr.setScore(rs.getInt("score"));
                sr.setRanking(rs.getInt("ranking"));
                sr.setShortlistStatus(rs.getString("shortlist_status"));
                sr.setRemarks(safeGetString(rs, "remarks", ""));
                results.add(sr);
            }
        }
        return results;
    }

    public List<InterviewSchedule> getAllInterviewSchedules() throws SQLException {
        List<InterviewSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM interview_schedules";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                InterviewSchedule is = new InterviewSchedule();
                is.setScheduleId(rs.getString("schedule_id"));
                is.setCandidateId(rs.getString("candidate_id"));
                is.setInterviewerId(rs.getString("interviewer_id"));
                Date iDate = rs.getDate("interview_date");
                if (iDate != null) is.setInterviewDate(iDate.toLocalDate());
                Time iTime = rs.getTime("interview_time");
                if (iTime != null) is.setInterviewTime(iTime.toLocalTime());
                is.setInterviewType(rs.getString("interview_type"));
                schedules.add(is);
            }
        }
        return schedules;
    }

    public List<Offer> getAllOffers() throws SQLException {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT * FROM offers";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Offer o = new Offer();
                o.setOfferId(rs.getString("offer_id"));
                o.setCandidateId(rs.getString("candidate_id"));
                o.setOfferDetails(rs.getString("offer_details"));
                o.setSalary(rs.getBigDecimal("salary"));
                Date sDate = rs.getDate("start_date");
                if (sDate != null) o.setStartDate(sDate.toLocalDate());
                o.setStatus(rs.getString("status"));
                Date exDate = rs.getDate("expiry_date");
                if (exDate != null) o.setExpiryDate(exDate.toLocalDate());
                offers.add(o);
            }
        }
        return offers;
    }

    public List<NotificationLog> getAllNotificationLogs() throws SQLException {
        List<NotificationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM notification_logs";
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NotificationLog nl = new NotificationLog();
                nl.setNotificationId(rs.getString("notification_id"));
                nl.setNotificationType(rs.getString("notification_type"));
                nl.setSentAds(rs.getString("sent_ads"));
                nl.setStatusAlert(rs.getString("status_alert"));
                nl.setContactInfoUsed(rs.getString("contact_info_used"));
                Timestamp sTime = rs.getTimestamp("sent_timestamp");
                if (sTime != null) nl.setSentTimestamp(sTime.toLocalDateTime());
                logs.add(nl);
            }
        }
        return logs;
    }
    public void saveAuditEntry(AuditEntry entry) throws SQLException {
        entry.validate();
        String sql = "INSERT INTO audit_log (timestamp, action, actor, details) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(entry.getTimestamp()));
            statement.setString(2, entry.getAction());
            statement.setString(3, entry.getActor());
            statement.setString(4, entry.getDetails());
            statement.executeUpdate();
        }
    }

    public List<AuditEntry> getAllAuditEntries() throws SQLException {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC";
        // Check if table exists first to avoid crash on first run before schema update
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Minimal check for table existence
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    AuditEntry e = new AuditEntry();
                    e.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    e.setAction(rs.getString("action"));
                    e.setActor(rs.getString("actor"));
                    e.setDetails(rs.getString("details"));
                    entries.add(e);
                }
            } catch (SQLException ex) {
                // Table might not exist yet, return empty list or consider creating it
                if (ex.getMessage().contains("no such table")) {
                    createAuditLogTable(conn);
                } else {
                    throw ex;
                }
            }
        }
        return entries;
    }

    private void createAuditLogTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS audit_log (" +
                "timestamp TIMESTAMP NOT NULL, " +
                "action VARCHAR(100) NOT NULL, " +
                "actor VARCHAR(100) NOT NULL, " +
                "details TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private String safeGetString(ResultSet rs, String columnName, String defaultValue) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private int safeGetInt(ResultSet rs, String columnName, int defaultValue) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException e) {
            return defaultValue;
        }
    }
}
