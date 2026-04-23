package com.springbooters.recruitment.exception;

/**
 * Base exception for all Recruitment Management System exceptions.
 *
 * GRASP - Protected Variations: All exceptions extend this base class,
 * shielding callers from changes in specific exception types.
 *
 * SOLID - Open/Closed Principle: New exception types can be added by
 * extending this class without modifying existing exception-handling code.
 */
public class RecruitmentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final ExceptionCategory category;

    public enum ExceptionCategory {
        MAJOR, MINOR, WARNING
    }

    public RecruitmentException(String errorCode, String message, ExceptionCategory category) {
        super(message);
        this.errorCode = errorCode;
        this.category = category;
    }

    // Optional constructor with cause (useful for wrapping exceptions)
    public RecruitmentException(String errorCode, String message,
                                ExceptionCategory category, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.category = category;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ExceptionCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "RecruitmentException{errorCode='" + errorCode
                + "', category=" + category
                + ", message='" + getMessage() + "'}";
    }
}