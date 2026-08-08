package com.example.ZeptoSupportTicketManager.responses;

import com.example.ZeptoSupportTicketManager.enums.ActionType;

public class ActionResult {

    private boolean success;
    private ActionType action;
    private String message;
    private Double amount;

    public ActionResult(boolean success, ActionType action, String message, Double amount) {
        this.success = success;
        this.action = action;
        this.message = message;
        this.amount = amount;
    }

    public boolean isSuccess() {
        return success;
    }

    public ActionType getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public Double getAmount() {
        return amount;
    }
}
