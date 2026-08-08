package com.example.ZeptoSupportTicketManager.dto;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import jakarta.validation.constraints.NotNull;

public class HumanReviewRequest {

    @NotNull(message = "action is required")
    private ActionType action;

    private String reviewNote;

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
