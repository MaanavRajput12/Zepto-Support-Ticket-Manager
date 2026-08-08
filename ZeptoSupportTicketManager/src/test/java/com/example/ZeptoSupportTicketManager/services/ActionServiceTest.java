package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActionServiceTest {

    private final ActionService actionService = new ActionService();

    @Test
    void refundNeverExceedsOrderValue() {
        OrderContext order = new OrderContext(1L, "groceries", 300.0, "10:00", "DELIVERED");
        List<SimilarTicketResponse> precedents = List.of(
                new SimilarTicketResponse(99L, "refund issue", 95.0, ActionType.REFUND,
                        "Refunded Rs 500 for missing items", 4.7));

        ActionResult result = actionService.execute(ActionType.REFUND, order, precedents);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAmount()).isEqualTo(300.0);
    }
}
