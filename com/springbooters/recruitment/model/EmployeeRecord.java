package com.springbooters.recruitment.model;

import java.time.LocalDate;

/**
 * Created when a hired candidate transitions to an employee.
 * Synced with the Main Employee Database after creation.
 *
 * SOLID - Single Responsibility: Represents only employee onboarding data.
 * The sync operation is handled by OfferService + SyncFailureException.
 */
public class EmployeeRecord {

    private String employeeId;
    private String candidateId;     // Traceability back to original application
    private String employeeName;
    private String department;
    private String designation;
    private LocalDate joiningDate;

    public EmployeeRecord() {}

    // Optional constructor for cleaner object creation
    public EmployeeRecord(String employeeId, String candidateId, String employeeName,
                          String department, String designation, LocalDate joiningDate) {
        this.employeeId = employeeId;
        this.candidateId = candidateId;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
    }

    /**
     * Basic validation to ensure required fields are present.
     */
    public void validate() {
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalStateException("Employee name is required");
        }
        if (candidateId == null || candidateId.trim().isEmpty()) {
            throw new IllegalStateException("Candidate ID is required");
        }
        if (joiningDate == null) {
            throw new IllegalStateException("Joining date is required");
        }
    }

    public String getEmployeeId()                       { return employeeId; }
    public void setEmployeeId(String employeeId)        { this.employeeId = employeeId; }

    public String getCandidateId()                      { return candidateId; }
    public void setCandidateId(String candidateId)      { this.candidateId = candidateId; }

    public String getEmployeeName()                     { return employeeName; }
    public void setEmployeeName(String employeeName)    { this.employeeName = employeeName; }

    public String getDepartment()                       { return department; }
    public void setDepartment(String department)        { this.department = department; }

    public String getDesignation()                      { return designation; }
    public void setDesignation(String designation)      { this.designation = designation; }

    public LocalDate getJoiningDate()                   { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate)   { this.joiningDate = joiningDate; }

    @Override
    public String toString() {
        return "EmployeeRecord{employeeId='" + employeeId
                + "', name='" + employeeName
                + "', department='" + department + "'}";
    }
}