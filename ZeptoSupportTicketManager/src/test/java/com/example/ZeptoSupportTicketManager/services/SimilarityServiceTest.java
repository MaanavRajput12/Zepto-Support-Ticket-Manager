package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ZeptoSupportTicketManager.entities.ResolvedTicket;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimilarityServiceTest {

    private final SimilarityService similarityService = new SimilarityService(null);

    @Test
    void highSimilarityMatchingRanksMissingMilkFirst() {
        List<ResolvedTicket> history = List.of(
                new ResolvedTicket(1L, "Milk packet missing from order", ActionType.REFUND, "Refunded Rs 40", 4.8),
                new ResolvedTicket(2L, "Delivery partner was late", ActionType.COUPON, "Coupon issued", 4.0),
                new ResolvedTicket(3L, "Ice cream melted and damaged", ActionType.REDELIVERY, "Redelivery", 4.2));

        List<SimilarTicketResponse> results = similarityService.rankTickets("Milk packet missing", history, 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getTicketId()).isEqualTo(1L);
        assertThat(results.get(0).getSimilarity()).isGreaterThan(70.0);
    }
}
