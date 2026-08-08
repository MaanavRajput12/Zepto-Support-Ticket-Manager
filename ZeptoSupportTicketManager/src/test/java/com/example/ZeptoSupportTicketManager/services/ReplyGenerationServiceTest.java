package com.example.ZeptoSupportTicketManager.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import org.junit.jupiter.api.Test;

class ReplyGenerationServiceTest {

    private final ReplyGenerationService replyGenerationService = new ReplyGenerationService();
    private final NewTicket ticket = new NewTicket(1L, "Milk missing", 10L);
    private final OrderContext order = new OrderContext(10L, "Milk", 120.0, "10:00", "DELIVERED");

    @Test
    void autoResolvedReplyClaimsOnlyExecutedRefund() {
        String reply = replyGenerationService.generateReply(ticket, order,
                new DecisionResult(DecisionType.AUTO_RESOLVED, ActionType.REFUND, 90.0, "reason"),
                new ActionResult(true, ActionType.REFUND, "done", 40.0));

        assertThat(reply).contains("initiated a refund of Rs 40.0");
    }

    @Test
    void humanReviewReplyDoesNotClaimActionHappened() {
        String reply = replyGenerationService.generateReply(ticket, order,
                new DecisionResult(DecisionType.HUMAN_REVIEW, ActionType.NONE, 30.0, "reason"),
                new ActionResult(false, ActionType.NONE, "No action", null));

        assertThat(reply).contains("forwarded to a support specialist");
        assertThat(reply).doesNotContain("initiated");
    }
}
