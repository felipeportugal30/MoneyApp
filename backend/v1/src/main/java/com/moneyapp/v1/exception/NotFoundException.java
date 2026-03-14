package com.moneyapp.v1.exception;

public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
}
