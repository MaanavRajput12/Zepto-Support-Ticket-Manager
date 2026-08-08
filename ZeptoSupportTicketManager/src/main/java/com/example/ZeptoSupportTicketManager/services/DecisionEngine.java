package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DecisionEngine {

    static final double TOP_SIMILARITY_THRESHOLD = 70.0;
    static final double HIGH_CONFIDENCE_THRESHOLD = 75.0;

    public DecisionResult decide(NewTicket ticket, List<SimilarTicketResponse> precedents, OrderContext order) {
        if (precedents == null || precedents.isEmpty()) {
            return humanReview(0.0, "Escalated because no historical precedents are available for ticket "
                    + ticket.getId() + ".");
        }

        double topSimilarity = precedents.get(0).getSimilarity();
        double averageSimilarity = precedents.stream().mapToDouble(SimilarTicketResponse::getSimilarity).average()
                .orElse(0.0);
        Map<ActionType, Long> actionCounts = precedents.stream()
                .collect(Collectors.groupingBy(SimilarTicketResponse::getAction, Collectors.counting()));
        long largestAgreement = actionCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        double agreementRatio = largestAgreement / (double) precedents.size();

        // Formula: 50% top match strength, 30% average evidence strength, 20% action agreement.
        double confidence = round(topSimilarity * 0.5 + averageSimilarity * 0.3 + agreementRatio * 100.0 * 0.2);
        Set<ActionType> actions = actionCounts.keySet();
        ActionType selectedAction = actionCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActionType.NONE);

        if (topSimilarity < TOP_SIMILARITY_THRESHOLD) {
            return humanReview(confidence, "Escalated because the best historical match is only "
                    + topSimilarity + "%. Precedents: " + precedentSummary(precedents) + ".");
        }

        if (actions.size() > 1) {
            return humanReview(confidence, "Escalated because historical precedents disagree on the action. "
                    + "Observed actions: " + actions + ". Precedents: " + precedentSummary(precedents) + ".");
        }

        if (!EnumSet.of(ActionType.REFUND, ActionType.REDELIVERY, ActionType.COUPON).contains(selectedAction)) {
            return humanReview(confidence, "Escalated because the agreed historical action is " + selectedAction
                    + ", so no automatic customer action is safe. Precedents: " + precedentSummary(precedents) + ".");
        }

        if (!isActionCompatible(selectedAction, order)) {
            return humanReview(confidence, "Escalated because " + selectedAction + " is not safe for order "
                    + order.getId() + " with status " + order.getStatus() + ". Precedents: "
                    + precedentSummary(precedents) + ".");
        }

        if (confidence < HIGH_CONFIDENCE_THRESHOLD) {
            return humanReview(confidence, "Escalated because confidence " + confidence
                    + "% is below the auto-resolution threshold. Precedents: " + precedentSummary(precedents) + ".");
        }

        String reasoning = "Auto-resolved because the top historical precedents all agree on " + selectedAction
                + ". Top similarity: " + topSimilarity + "%. Average similarity: " + round(averageSimilarity)
                + "%. Confidence: " + confidence + "%. Order value: Rs " + order.getValue()
                + ". Precedents: " + precedentSummary(precedents) + ".";
        return new DecisionResult(DecisionType.AUTO_RESOLVED, selectedAction, confidence, reasoning);
    }

    private boolean isActionCompatible(ActionType action, OrderContext order) {
        if (action == ActionType.REDELIVERY && order.getStatus() != null
                && order.getStatus().toUpperCase(Locale.ROOT).contains("CANCEL")) {
            return false;
        }
        return true;
    }

    private DecisionResult humanReview(double confidence, String reasoning) {
        return new DecisionResult(DecisionType.HUMAN_REVIEW, ActionType.NONE, round(confidence), reasoning);
    }

    private String precedentSummary(List<SimilarTicketResponse> precedents) {
        return precedents.stream()
                .map(precedent -> "#" + precedent.getTicketId() + " " + precedent.getAction()
                        + " (" + precedent.getSimilarity() + "%)")
                .collect(Collectors.joining(", "));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
