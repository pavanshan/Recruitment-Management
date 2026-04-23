package com.springbooters.recruitment.model;

/**
 * Represents a job applicant/candidate in the system.
 * Stores personal details, resume, and skill information.
 *
 * GRASP - Information Expert: Candidate owns its own profile data
 * and is responsible for providing it when queried.
 *
 * SOLID - Single Responsibility: Only models candidate data.
 */
public class Candidate {

    private String candidateId;
    private String name;
    private String contactInfo;   // email or phone
    private String resume;        // file path or encoded resume content
    private String skills;        // comma-separated list of skills
    private String source;        // LinkedIn, Referral, Direct, Naukri, etc.
    private String referredBy;    // Name of the person who referred this candidate
    private int experience;       // years of experience

    public Candidate() {}

    /** Returns true if the candidate has a resume on file. */
    public boolean hasResume() {
        return resume != null && !resume.trim().isEmpty();
    }

    /**
     * Basic validation for mandatory candidate fields.
     * Helps prevent invalid candidate objects.
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Candidate name is required");
        }

        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            throw new IllegalStateException("Contact information is required");
        }
    }

    public String getCandidateId()              { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getContactInfo()              { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getResume()                   { return resume; }
    public void setResume(String resume)        { this.resume = resume; }

    public String getSkills()                   { return skills; }
    public void setSkills(String skills)        { this.skills = skills; }

    public String getSource()                   { return source; }
    public void setSource(String source)        { this.source = source; }

    public String getReferredBy()               { return referredBy; }
    public void setReferredBy(String referredBy){ this.referredBy = referredBy; }

    public int getExperience()                  { return experience; }
    public void setExperience(int experience)   { this.experience = experience; }

    @Override
    public String toString() {
        return "Candidate{candidateId='" + candidateId
                + "', name='" + name
                + "', source='" + source + "'}";
    }
}