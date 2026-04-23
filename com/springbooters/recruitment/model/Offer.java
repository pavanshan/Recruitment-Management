package com.springbooters.recruitment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a job offer extended to a successful candidate.
 *
 * GRASP - Information Expert: Offer knows whether it's expired or accepted.
 * Built via OfferBuilder (Creational - Builder Pattern) due to many optional fields.
 */
public class Offer {

    private String offerId;
    private String candidateId;
    private String offerDetails;    // Brief summary: role, location, benefits
    private BigDecimal salary;
    private LocalDate startDate;
    private String status;          // PENDING, ACCEPTED, REJECTED, EXPIRED
    private LocalDate expiryDate;

    public Offer() {}

    /**
     * Returns true if the offer can no longer be accepted —
     * either its deadline has passed or it was explicitly marked EXPIRED.
     */
    public boolean isExpired() {
        boolean pastDeadline = expiryDate != null && LocalDate.now().isAfter(expiryDate);
        return pastDeadline || "EXPIRED".equalsIgnoreCase(status);
    }

    /** Accept the offer */
    public void accept() {
        if (isExpired()) {
            throw new IllegalStateException("Cannot accept an expired offer");
        }
        this.status = "ACCEPTED";
    }

    /** Reject the offer */
    public void reject() {
        this.status = "REJECTED";
    }

    /**
     * Basic validation for offer integrity.
     */
    public void validate() {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            throw new IllegalStateException("Candidate ID is required");
        }

        OfferStatus.from(status);
        if (salary == null) {
            throw new IllegalStateException("Offer salary is required");
        }
    }

    public String getOfferId()              { return offerId; }
    public void setOfferId(String offerId)  { this.offerId = offerId; }

    public String getCandidateId()                  { return candidateId; }
    public void setCandidateId(String candidateId)  { this.candidateId = candidateId; }

    public String getOfferDetails()                     { return offerDetails; }
    public void setOfferDetails(String offerDetails)    { this.offerDetails = offerDetails; }

    public BigDecimal getSalary()               { return salary; }

    public void setSalary(BigDecimal salary) {
        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    public LocalDate getStartDate()                 { return startDate; }
    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }

    public String getStatus()               { return status; }

    public void setStatus(String status) {
        this.status = OfferStatus.from(status).name();
    }

    public LocalDate getExpiryDate()                    { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate)     { this.expiryDate = expiryDate; }

    @Override
    public String toString() {
        return "Offer{offerId='" + offerId
                + "', candidateId='" + candidateId
                + "', status='" + status
                + "', salary=" + salary + "}";
    }
}
