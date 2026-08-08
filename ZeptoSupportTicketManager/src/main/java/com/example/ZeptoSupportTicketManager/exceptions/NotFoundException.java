package com.example.ZeptoSupportTicketManager.exceptions;

public class NotFoundException extends RuntimeException {

    private final String error;

    public NotFoundException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
