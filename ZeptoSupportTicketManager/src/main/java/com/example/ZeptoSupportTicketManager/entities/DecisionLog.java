package com.example.ZeptoSupportTicketManager.entities;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "decision_logs")
public class DecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType decision;

    @Enumerated(EnumType.STRING)
    @Column
    private ActionType suggestedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType selectedAction;

    @Enumerated(EnumType.STRING)
    @Column
    private ActionType executedAction;

    @Column(columnDefinition = "TEXT")
    private String actionMessage;

    private Double actionAmount;

    @Column(columnDefinition = "TEXT")
    private String precedentIds;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reasoning;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String draftedReply;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public void setDecision(DecisionType decision) {
        this.decision = decision;
    }

    public ActionType getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(ActionType selectedAction) {
        this.selectedAction = selectedAction;
        this.suggestedAction = selectedAction;
    }

    public ActionType getSuggestedAction() {
        return suggestedAction == null ? selectedAction : suggestedAction;
    }

    public void setSuggestedAction(ActionType suggestedAction) {
        this.suggestedAction = suggestedAction;
        this.selectedAction = suggestedAction;
    }

    public ActionType getExecutedAction() {
        return executedAction;
    }

    public void setExecutedAction(ActionType executedAction) {
        this.executedAction = executedAction;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public Double getActionAmount() {
        return actionAmount;
    }

    public void setActionAmount(Double actionAmount) {
        this.actionAmount = actionAmount;
    }

    public String getPrecedentIds() {
        return precedentIds;
    }

    public void setPrecedentIds(String precedentIds) {
        this.precedentIds = precedentIds;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getDraftedReply() {
        return draftedReply;
    }

    public void setDraftedReply(String draftedReply) {
        this.draftedReply = draftedReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
