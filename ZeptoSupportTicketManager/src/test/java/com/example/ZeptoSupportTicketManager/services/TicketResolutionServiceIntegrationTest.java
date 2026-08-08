package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(decisionLogRepository.findTopByTicketIdOrderByCreatedAtDesc(ticket.getId())).isPresent();
    }
}
