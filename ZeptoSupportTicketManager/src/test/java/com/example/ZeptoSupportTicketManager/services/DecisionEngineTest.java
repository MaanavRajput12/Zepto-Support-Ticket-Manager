package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecisionEngineTest {

    private final DecisionEngine decisionEngine = new DecisionEngine();
    private final NewTicket ticket = new NewTicket(101L, "Milk packet missing", 123L);
    private final OrderContext deliveredOrder = new OrderContext(123L, "Milk", 280.0, "10:00", "DELIVERED");

    @Test
    void strongPrecedentAgreementAutoResolves() {
        DecisionResult result = decisionEngine.decide(ticket, List.of(
                precedent(1L, 96.0, ActionType.REFUND),
                precedent(2L, 92.0, ActionType.REFUND),
                precedent(3L, 88.0, ActionType.REFUND)), deliveredOrder);

        assertThat(result.getDecision()).isEqualTo(DecisionType.AUTO_RESOLVED);
        assertThat(result.getSelectedAction()).isEqualTo(ActionType.REFUND);
        assertThat(result.getConfidence()).isGreaterThanOrEqualTo(75.0);
        assertThat(result.getReasoning()).contains("#1", "#2", "#3");
    }

    @Test
    void lowSimilarityEscalatesToHumanReview() {
        DecisionResult result = decisionEngine.decide(ticket, List.of(
                precedent(1L, 42.0, ActionType.REFUND),
                precedent(2L, 31.0, ActionType.REFUND),
                precedent(3L, 28.0, ActionType.REFUND)), deliveredOrder);

        assertThat(result.getDecision()).isEqualTo(DecisionType.HUMAN_REVIEW);
        assertThat(result.getSelectedAction()).isEqualTo(ActionType.NONE);
    }

    @Test
    void conflictingPrecedentsEscalateToHumanReview() {
        DecisionResult result = decisionEngine.decide(ticket, List.of(
                precedent(1L, 96.0, ActionType.REFUND),
                precedent(2L, 91.0, ActionType.COUPON),
                precedent(3L, 89.0, ActionType.REFUND)), deliveredOrder);

        assertThat(result.getDecision()).isEqualTo(DecisionType.HUMAN_REVIEW);
        assertThat(result.getReasoning()).contains("disagree");
    }

    @Test
    void cancelledOrderCannotTriggerRedelivery() {
        OrderContext cancelledOrder = new OrderContext(125L, "Ice cream", 300.0, "10:00", "CANCELLED");

        DecisionResult result = decisionEngine.decide(ticket, List.of(
                precedent(1L, 96.0, ActionType.REDELIVERY),
                precedent(2L, 92.0, ActionType.REDELIVERY),
                precedent(3L, 88.0, ActionType.REDELIVERY)), cancelledOrder);

        assertThat(result.getDecision()).isEqualTo(DecisionType.HUMAN_REVIEW);
        assertThat(result.getSelectedAction()).isEqualTo(ActionType.NONE);
        assertThat(result.getReasoning()).contains("not safe");
    }

    private SimilarTicketResponse precedent(Long id, double similarity, ActionType action) {
        return new SimilarTicketResponse(id, "similar issue", similarity, action, "resolved", 4.5);
    }
}
