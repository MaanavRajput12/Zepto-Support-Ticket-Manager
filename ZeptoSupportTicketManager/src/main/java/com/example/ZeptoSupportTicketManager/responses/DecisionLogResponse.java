package com.example.ZeptoSupportTicketManager.responses;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import java.time.LocalDateTime;

public class DecisionLogResponse {

    private Long id;
    private Long ticketId;
    private Double confidence;
    private DecisionType decision;
    private ActionType suggestedAction;
    private ActionType selectedAction;
    private ActionType executedAction;
    private String actionMessage;
    private Double actionAmount;
    private String precedentIds;
    private String reviewNote;
    private String reasoning;
    private String draftedReply;
    private LocalDateTime createdAt;

    public DecisionLogResponse(Long id, Long ticketId, Double confidence, DecisionType decision,
            ActionType suggestedAction, ActionType executedAction, String actionMessage, Double actionAmount,
            String precedentIds, String reviewNote, String reasoning, String draftedReply, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.confidence = confidence;
        this.decision = decision;
        this.suggestedAction = suggestedAction;
        this.selectedAction = suggestedAction;
        this.executedAction = executedAction;
        this.actionMessage = actionMessage;
        this.actionAmount = actionAmount;
        this.precedentIds = precedentIds;
        this.reviewNote = reviewNote;
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

    public ActionType getSuggestedAction() {
        return suggestedAction;
    }

    public ActionType getSelectedAction() {
        return selectedAction;
    }

    public ActionType getExecutedAction() {
        return executedAction;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public Double getActionAmount() {
        return actionAmount;
    }

    public String getPrecedentIds() {
        return precedentIds;
    }

    public String getReviewNote() {
        return reviewNote;
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
