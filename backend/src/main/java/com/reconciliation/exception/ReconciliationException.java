package com.reconciliation.exception;

public class ReconciliationException extends RuntimeException {
    
    private String errorCode;
    
    public ReconciliationException(String message) {
        super(message);
    }
    
    public ReconciliationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ReconciliationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

