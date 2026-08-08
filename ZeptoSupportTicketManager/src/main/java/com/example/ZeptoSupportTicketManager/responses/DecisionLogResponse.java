package com.example.ZeptoSupportTicketManager.responses;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import java.time.LocalDateTime;

public class DecisionLogResponse {

    private Long id;
    private Long ticketId;
    private Double confidence;
    private DecisionType decision;
    private ActionType selectedAction;
    private String reasoning;
    private String draftedReply;
    private LocalDateTime createdAt;

    public DecisionLogResponse(Long id, Long ticketId, Double confidence, DecisionType decision,
            ActionType selectedAction, String reasoning, String draftedReply, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.confidence = confidence;
        this.decision = decision;
        this.selectedAction = selectedAction;
        this.reasoning = reasoning;
        this.draftedReply = draftedReply;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public ActionType getSelectedAction() {
        return selectedAction;
    }

    public String getReasoning() {
        return reasoning;
    }

    public String getDraftedReply() {
        return draftedReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
