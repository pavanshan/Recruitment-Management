CREATE DATABASE IF NOT EXISTS recruitment_db;
USE recruitment_db;

CREATE TABLE IF NOT EXISTS candidates (
    candidate_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_info VARCHAR(120) NOT NULL,
    resume VARCHAR(255),
    skills VARCHAR(500),
    source VARCHAR(80),
    experience INT
);

CREATE TABLE IF NOT EXISTS job_postings (
    job_id VARCHAR(30) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    department VARCHAR(100) NOT NULL,
    description TEXT,
    salary DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL,
    platform_name VARCHAR(100),
    channel_type VARCHAR(20),
    posted_date DATE,
    expiry_date DATE,
    experience INT,
    min_screening_score INT,
    required_skills VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS applications (
    application_id VARCHAR(30) PRIMARY KEY,
    candidate_id VARCHAR(30) NOT NULL,
    job_id VARCHAR(30) NOT NULL,
    date_applied DATE NOT NULL,
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id),
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id)
);

CREATE TABLE IF NOT EXISTS application_statuses (
    application_id VARCHAR(30) PRIMARY KEY,
    current_stage VARCHAR(30) NOT NULL,
    history TEXT,
    updated_at TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(application_id)
);

CREATE TABLE IF NOT EXISTS screening_results (
    application_id VARCHAR(30) PRIMARY KEY,
    score INT NOT NULL,
    ranking INT NOT NULL,
    shortlist_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    FOREIGN KEY (application_id) REFERENCES applications(application_id)
);

CREATE TABLE IF NOT EXISTS interview_schedules (
    schedule_id VARCHAR(30) PRIMARY KEY,
    candidate_id VARCHAR(30) NOT NULL,
    interviewer_id VARCHAR(30) NOT NULL,
    interview_date DATE NOT NULL,
    interview_time TIME NOT NULL,
    interview_type VARCHAR(30) NOT NULL,
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE TABLE IF NOT EXISTS offers (
    offer_id VARCHAR(30) PRIMARY KEY,
    candidate_id VARCHAR(30) NOT NULL,
    offer_details VARCHAR(500),
    salary DECIMAL(12, 2) NOT NULL,
    start_date DATE,
    status VARCHAR(30) NOT NULL,
    expiry_date DATE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE TABLE IF NOT EXISTS notification_logs (
    notification_id VARCHAR(30) PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    sent_ads TEXT,
    status_alert TEXT,
    contact_info_used VARCHAR(120) NOT NULL,
    sent_timestamp TIMESTAMP NOT NULL
);
