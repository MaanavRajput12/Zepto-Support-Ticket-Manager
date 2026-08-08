package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;

public class DecisionResult {

    private final DecisionType decision;
    private final ActionType selectedAction;
    private final Double confidence;
    private final String reasoning;

    public DecisionResult(DecisionType decision, ActionType selectedAction, Double confidence, String reasoning) {
        this.decision = decision;
        this.selectedAction = selectedAction;
        this.confidence = confidence;
        this.reasoning = reasoning;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public ActionType getSelectedAction() {
        return selectedAction;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getReasoning() {
        return reasoning;
    }
}
