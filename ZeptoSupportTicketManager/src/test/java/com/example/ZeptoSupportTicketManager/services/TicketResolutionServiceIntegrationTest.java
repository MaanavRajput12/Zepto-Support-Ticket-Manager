package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ZeptoSupportTicketManager.dto.HumanReviewRequest;
import com.example.ZeptoSupportTicketManager.entities.DecisionLog;
import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.entities.ResolvedTicket;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.repositories.DecisionLogRepository;
import com.example.ZeptoSupportTicketManager.repositories.NewTicketRepository;
import com.example.ZeptoSupportTicketManager.repositories.OrderRepository;
import com.example.ZeptoSupportTicketManager.repositories.ResolvedTicketRepository;
import com.example.ZeptoSupportTicketManager.responses.TicketResolutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketResolutionServiceIntegrationTest {

    @Autowired
    private TicketResolutionService ticketResolutionService;

    @Autowired
    private NewTicketRepository newTicketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ResolvedTicketRepository resolvedTicketRepository;

    @Autowired
    private DecisionLogRepository decisionLogRepository;

    @BeforeEach
    void setUp() {
        decisionLogRepository.deleteAll();
        newTicketRepository.deleteAll();
        resolvedTicketRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void resolveTicketCreatesDecisionLog() {
        orderRepository.save(new OrderContext(123L, "Milk, bread", 280.0, "10:00", "DELIVERED"));
        resolvedTicketRepository.save(new ResolvedTicket(null, "Milk packet missing from order", ActionType.REFUND,
                "Refunded Rs 40", 4.8));
        resolvedTicketRepository.save(new ResolvedTicket(null, "Missing milk item in order", ActionType.REFUND,
                "Refund issued Rs 40", 4.7));
        resolvedTicketRepository.save(new ResolvedTicket(null, "Milk not delivered", ActionType.REFUND,
                "Refunded Rs 35", 4.6));
        NewTicket ticket = newTicketRepository.save(new NewTicket(null, "Milk packet missing", 123L));

        TicketResolutionResponse response = ticketResolutionService.resolveTicket(ticket.getId());

        assertThat(response.getDecision()).isEqualTo(DecisionType.AUTO_RESOLVED);
        assertThat(response.getSuggestedAction()).isEqualTo(ActionType.REFUND);
        assertThat(response.getExecutedAction()).isEqualTo(ActionType.REFUND);
        assertThat(response.getOrder().getValue()).isEqualTo(280.0);
        assertThat(decisionLogRepository.findTopByTicketIdOrderByCreatedAtDesc(ticket.getId())).isPresent();
    }

    @Test
    void approveHumanReviewExecutesSuggestedActionAfterSafetyCheck() {
        orderRepository.save(new OrderContext(321L, "Tea", 150.0, "20", "DELIVERED"));
        resolvedTicketRepository.save(new ResolvedTicket(null, "tea packet damaged", ActionType.REFUND,
                "Refunded Rs 20", 4.5));
        NewTicket ticket = newTicketRepository.save(new NewTicket(null, "tea packet damaged but customer wants review", 321L));
        DecisionLog aiDecisionLog = new DecisionLog();
        aiDecisionLog.setTicketId(ticket.getId());
        aiDecisionLog.setConfidence(62.0);
        aiDecisionLog.setDecision(DecisionType.HUMAN_REVIEW);
        aiDecisionLog.setSuggestedAction(ActionType.REFUND);
        aiDecisionLog.setExecutedAction(ActionType.NONE);
        aiDecisionLog.setReasoning("Escalated for human confirmation.");
        aiDecisionLog.setDraftedReply("Forwarded to support.");
        decisionLogRepository.save(aiDecisionLog);

        TicketResolutionResponse approved = ticketResolutionService.approveHumanReview(ticket.getId());

        assertThat(approved.getDecision()).isEqualTo(DecisionType.HUMAN_REVIEW);
        assertThat(approved.getExecutedAction()).isEqualTo(ActionType.REFUND);
        assertThat(approved.getDraftedReply()).contains("support specialist");
    }

    @Test
    void humanOverrideCannotExecuteUnsafeRedeliveryForCancelledOrder() {
        orderRepository.save(new OrderContext(456L, "Ice cream", 200.0, "40", "CANCELLED"));
        resolvedTicketRepository.save(new ResolvedTicket(null, "ice cream melted", ActionType.REFUND,
                "Refunded Rs 80", 4.0));
        NewTicket ticket = newTicketRepository.save(new NewTicket(null, "ice cream arrived melted", 456L));
        ticketResolutionService.resolveTicket(ticket.getId());
        HumanReviewRequest request = new HumanReviewRequest();
        request.setAction(ActionType.REDELIVERY);
        request.setReviewNote("Try replacement");

        assertThatThrownBy(() -> ticketResolutionService.overrideHumanReview(ticket.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not safe");
    }
}
