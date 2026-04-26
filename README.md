# Recruitment Management System

A full-featured **Java desktop application** developed as a subsystem of a larger HRMS software that manages the end-to-end hiring lifecycle — from job requisition and candidate sourcing through automated screening, interview scheduling, offer management, and analytics. Built as an Object-Oriented Analysis & Design (OOAD) project demonstrating GoF Design Patterns, SOLID principles, and GRASP guidelines.

---

## Table of Contents

- [Features](#features)
- [Design Patterns](#design-patterns)
- [SOLID Principles](#solid-principles)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Technologies Used](#technologies-used)

---

## Features

| Area | Capability |
|---|---|
| **Job Management** | Submit requisitions (DRAFT → APPROVED → ACTIVE), publish to career portal, set expiry |
| **Candidate Pool** | Register candidates with skills, experience, resume, and referral source |
| **Career Portal** | Public-facing job board where candidates can browse and apply directly |
| **Automated Screening** | Rule-based engine scores candidates on skills, experience, and resume availability |
| **Application Pipeline** | Full audit trail: `APPLIED → SCREENED → SHORTLISTED → INTERVIEW_SCHEDULED → INTERVIEW_COMPLETED → OFFERED → HIRED / REJECTED` |
| **Interview Scheduling** | Schedule phone, technical, and on-site interviews; record outcomes |
| **Offer Management** | Generate offers via a fluent Builder, track acceptance/rejection with expiry dates |
| **Multi-Channel Notifications** | Email (SMTP) and SMS alerts dispatched through a factory |
| **Analytics Dashboard** | Live hiring funnel metrics and stage-level pipeline breakdown |
| **Audit Log** | Immutable record of every state transition and system action |

---

## Design Patterns

### Facade — `RecruitmentFacade`
`RecruitmentFacade` is the single entry point for all HR workflows. The GUI interacts only with the Facade; it never touches the repository, screener, or notification layer directly. Swapping any subsystem (e.g., changing the email provider) does not affect the UI.

```
RecruitmentSwingApp (GUI)
         │
         ▼
 RecruitmentFacade          ← one clean API for all operations
   ├── RecruitmentRepository
   ├── AutomatedScreener
   └── NotificationFactory
```

### Builder — `JobPostingBuilder` / `OfferBuilder`
Complex domain objects are assembled through a fluent API. Validation fires inside `build()`, so no invalid object can enter the system.

```java
JobPosting job = new JobPostingBuilder()
    .withJobId("JOB-001")
    .withTitle("Senior Backend Engineer")
    .withDepartment("Engineering")
    .withRequiredSkills("Java,SQL,Spring")
    .withMinScreeningScore(60)
    .withStatus(JobStatus.DRAFT)
    .build();
```

### Chain of Responsibility — `ScreeningRule`
The automated screener links rule objects into a chain. Each rule scores one aspect of the candidate and passes the `ScreeningReport` to the next link. New rules can be added without modifying any existing class.

```
ResumeAvailableRule → ExperienceMatchRule → SkillMatchRule
       (10 pts)             (40 pts)            (50 pts)
                                                    ▼
                                      Total score vs. minScreeningScore
```

```java
ResumeAvailableRule resumeRule = new ResumeAvailableRule();
resumeRule.linkWith(new ExperienceMatchRule())
          .linkWith(new SkillMatchRule());

AutomatedScreener screener = new AutomatedScreener(resumeRule);
```

### Factory Method — `NotificationFactory`
Resolves the correct `NotificationSender` from a channel string, decoupling dispatch logic from concrete transport implementations.

```java
NotificationSender sender = notificationFactory.createSender("EMAIL");
// or
NotificationSender sender = notificationFactory.createSender("SMS");
```

---

## SOLID Principles

| Principle | Application in this project |
|---|---|
| **Single Responsibility (SRP)** | `RecruitmentRepository` handles only SQL persistence; `AutomatedScreener` handles only rule execution; `ApplicationStatus` handles only stage tracking |
| **Open / Closed (OCP)** | New screening rules (e.g., background check) or notification channels (e.g., Slack) can be added by creating new subclasses — no existing class is modified |
| **Liskov Substitution (LSP)** | `SkillMatchRule`, `ExperienceMatchRule`, and `ResumeAvailableRule` are interchangeable as `ScreeningRule` objects inside `AutomatedScreener` |
| **Interface Segregation (ISP)** | `RecruitmentIntegration` exposes a focused external API for other HRMS modules (Payroll, Onboarding, Analytics) without leaking internal implementation details |
| **Dependency Inversion (DIP)** | `RecruitmentFacade` depends on `NotificationFactory` and `AutomatedScreener` abstractions — concrete SMTP or scoring implementations can be swapped freely |

---

## Project Structure

```
Recruitment-Management-main/
├── com/springbooters/recruitment/
│   │
│   ├── db/
│   │   ├── DatabaseConnection.java        # JDBC helper — SQLite default, MySQL via env vars
│   │   └── RecruitmentRepository.java     # All SQL CRUD operations
│   │
│   ├── model/
│   │   ├── Candidate.java
│   │   ├── JobPosting.java
│   │   ├── JobPostingBuilder.java          # Builder pattern
│   │   ├── JobStatus.java                 # DRAFT | APPROVED | ACTIVE | EXPIRED | CLOSED
│   │   ├── Application.java
│   │   ├── ApplicationStatus.java         # Pipeline stage tracker + history
│   │   ├── ApplicationStage.java          # Stage enum with transition validation
│   │   ├── ScreeningResult.java
│   │   ├── InterviewSchedule.java
│   │   ├── InterviewType.java             # PHONE | TECHNICAL | ONSITE
│   │   ├── Offer.java
│   │   ├── OfferBuilder.java              # Builder pattern
│   │   ├── OfferStatus.java               # PENDING | ACCEPTED | DECLINED | EXPIRED
│   │   ├── EmployeeRecord.java
│   │   ├── NotificationLog.java
│   │   ├── NotificationType.java
│   │   ├── AuditEntry.java
│   │   ├── ChannelType.java               # INTERNAL | EXTERNAL | REFERRAL
│   │   └── ShortlistStatus.java           # SHORTLISTED | REJECTED
│   │
│   ├── service/
│   │   ├── RecruitmentFacade.java         # Facade — main workflow coordinator
│   │   ├── RecruitmentIntegration.java    # External HRMS integration interface (ISP / DIP)
│   │   ├── AutomatedScreener.java         # Executes the screening rule chain
│   │   ├── ScreeningRule.java             # Abstract Chain of Responsibility node
│   │   ├── ScreeningReport.java           # Accumulates scores across the chain
│   │   ├── ResumeAvailableRule.java       # Rule: resume must be present (10 pts)
│   │   ├── ExperienceMatchRule.java       # Rule: years of experience vs. requirement (40 pts)
│   │   ├── SkillMatchRule.java            # Rule: candidate skills vs. required skills (50 pts)
│   │   ├── NotificationFactory.java       # Factory — creates EMAIL or SMS sender
│   │   ├── NotificationSender.java        # Sender interface
│   │   ├── EmailNotificationSender.java   # SMTP-based email sender
│   │   ├── SmtpEmailClient.java           # Low-level SMTP helper
│   │   └── SmsNotificationSender.java     # SMS sender stub
│   │
│   ├── gui/
│   │   ├── RecruitmentSwingApp.java       # Main Swing window (dark-themed dashboard)
│   │   └── AnalyticsPanel.java            # Hiring funnel & pipeline metrics panel
│   │
│   ├── exception/
│   │   └── RecruitmentException.java      # Domain exception with WARNING / ERROR category enum
│   │
│   └── test/
│       ├── WorkflowTest.java              # Happy-path full workflow validation
│       ├── PipelineEndToEndTest.java      # End-to-end pipeline from apply to hire
│       ├── StateTransitionTest.java       # Application stage transition rules
│       ├── VerifyPortalFlow.java          # Public career portal apply flow
│       ├── VerifyPublishAndApply.java     # Job publish + application submission
│       └── VerifyUserScenario.java        # Realistic multi-candidate HR scenario
│
├── database/
│   ├── schema.sql                         # Full DDL for all tables
│   └── hrms.db                            # Bundled SQLite database (ready to use)
│
└── lib/
    ├── sqlite-jdbc-3.45.1.0.jar
    ├── mysql-connector-j-8.3.0.jar
    ├── pdfbox-2.0.30.jar
    ├── fontbox-2.0.30.jar
    ├── slf4j-api-2.0.17.jar
    └── slf4j-simple-2.0.0-alpha6.jar
```

---

## Database Schema

The bundled SQLite database (`database/hrms.db`) is initialised automatically on first run using `database/schema.sql`.

| Table | Purpose |
|---|---|
| `candidates` | Candidate profiles — name, contact, skills, experience, resume path |
| `job_postings` | Job listings — title, department, salary, status, screening threshold, required skills |
| `applications` | Links a candidate to a job posting with the date applied |
| `application_statuses` | Current pipeline stage and full semicolon-delimited stage history with timestamps |
| `screening_results` | Score, ranking, and shortlist decision per application |
| `interview_schedules` | Scheduled slots — type, date, time, interviewer ID |
| `offers` | Salary offers — amount, start date, expiry, and acceptance status |
| `notification_logs` | Audit log of every outbound notification sent |

---

## Getting Started

### Prerequisites

- **Java 11** or later
- No build tool required — all dependencies are pre-bundled in `lib/`

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/Recruitment-Management.git
cd Recruitment-Management
```

### 2. Compile

**macOS / Linux**
```bash
javac -cp "lib/*" -d bin $(find com -name "*.java")
```

**Windows (Command Prompt)**
```cmd
dir /s /b com\*.java > sources.txt
javac -cp "lib\*" -d bin @sources.txt
```

### 3. Run the GUI

**macOS / Linux**
```bash
java -cp "bin:lib/*" com.springbooters.recruitment.gui.RecruitmentSwingApp
```

**Windows**
```cmd
java -cp "bin;lib\*" com.springbooters.recruitment.gui.RecruitmentSwingApp
```

The application opens with a dark-themed dashboard. Use the sidebar to navigate between **Jobs**, **Candidates**, **Applications**, **Pipeline**, **Interviews**, **Offers**, **Notifications**, and **Analytics**.

---

## Configuration

Database connection is controlled via environment variables. Without them, the app defaults to the bundled SQLite file at `database/hrms.db`.

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:sqlite:database/hrms.db` | JDBC connection URL |
| `DB_USER` | *(empty)* | Database username (required for MySQL) |
| `DB_PASSWORD` | *(empty)* | Database password (required for MySQL) |

**Example — switch to MySQL:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/recruitment_db"
export DB_USER="root"
export DB_PASSWORD="yourpassword"
java -cp "bin:lib/*" com.springbooters.recruitment.gui.RecruitmentSwingApp
```

---

## Application Pipeline

```
  [Job Requisition]
  DRAFT → APPROVED → ACTIVE
              │
              ▼
       Candidate Applies
              │
              ▼
           APPLIED
              │
    AutomatedScreener runs
    (Resume + Experience + Skills)
              │
              ▼
          SCREENED
         /        \
  passes score   fails score
        │               │
   SHORTLISTED      REJECTED
        │
  HR schedules interview
        │
        ▼
  INTERVIEW_SCHEDULED
        │
  Interview completed
        │
        ▼
  INTERVIEW_COMPLETED
        │
   HR records offer
        │
        ▼
      OFFERED
     /        \
  HIRED     REJECTED
```

---

## Technologies Used

- **Java** — Swing GUI, JDBC
- **SQLite** — default embedded database (`sqlite-jdbc-3.45.1.0`)
- **MySQL** — optional alternative database (`mysql-connector-j-8.3.0`)
- **Apache PDFBox 2.0.30** — PDF document handling
- **SLF4J** — logging facade

---

*Built as part of an Object-Oriented Analysis & Design (OOAD) course — demonstrating Facade, Builder, Chain of Responsibility, and Factory Method patterns alongside SOLID and GRASP compliance.*
