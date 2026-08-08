package com.example.ZeptoSupportTicketManager.responses;

import com.example.ZeptoSupportTicketManager.enums.ActionType;

public class SimilarTicketResponse {

    private Long ticketId;
    private String description;
    private Double similarity;
    private ActionType action;
    private String resolutionNote;
    private Double csat;

    public SimilarTicketResponse(Long ticketId, String description, Double similarity, ActionType action,
            String resolutionNote, Double csat) {
        this.ticketId = ticketId;
        this.description = description;
        this.similarity = similarity;
        this.action = action;
        this.resolutionNote = resolutionNote;
        this.csat = csat;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getDescription() {
        return description;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public ActionType getAction() {
        return action;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public Double getCsat() {
        return csat;
    }
}
