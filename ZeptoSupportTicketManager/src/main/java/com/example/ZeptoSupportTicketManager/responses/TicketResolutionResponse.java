package com.example.ZeptoSupportTicketManager.responses;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import java.util.List;

public class TicketResolutionResponse {

    private Long ticketId;
    private String description;
    private DecisionType decision;
    private Double confidence;
    private ActionType selectedAction;
    private ActionResult actionResult;
    private List<SimilarTicketResponse> topPrecedents;
    private String reasoning;
    private String draftedReply;

    public TicketResolutionResponse(Long ticketId, String description, DecisionType decision, Double confidence,
            ActionType selectedAction, ActionResult actionResult, List<SimilarTicketResponse> topPrecedents,
            String reasoning, String draftedReply) {
        this.ticketId = ticketId;
        this.description = description;
        this.decision = decision;
        this.confidence = confidence;
        this.selectedAction = selectedAction;
        this.actionResult = actionResult;
        this.topPrecedents = topPrecedents;
        this.reasoning = reasoning;
        this.draftedReply = draftedReply;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getDescription() {
        return description;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public Double getConfidence() {
        return confidence;
    }

    public ActionType getSelectedAction() {
        return selectedAction;
    }

    public ActionResult getActionResult() {
        return actionResult;
    }

    public List<SimilarTicketResponse> getTopPrecedents() {
        return topPrecedents;
    }

    public String getReasoning() {
        return reasoning;
    }

    public String getDraftedReply() {
        return draftedReply;
    }
}
