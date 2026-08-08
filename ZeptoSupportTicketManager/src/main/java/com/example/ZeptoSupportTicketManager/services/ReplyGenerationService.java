package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import org.springframework.stereotype.Service;

@Service
public class ReplyGenerationService {

    public String generateReply(NewTicket ticket, OrderContext order, DecisionResult decision, ActionResult actionResult) {
        if (decision.getDecision() == DecisionType.HUMAN_REVIEW) {
            return "We're sorry about the issue with your order. Your case has been forwarded to a support "
                    + "specialist for review.";
        }
        ActionType action = decision.getSelectedAction();
        if (action == ActionType.REFUND) {
            return "Sorry about the issue with your order. We have initiated a refund of Rs "
                    + actionResult.getAmount()
                    + ". You should see the refund according to the applicable processing timeline.";
        }
        if (action == ActionType.REDELIVERY) {
            return "Sorry about the issue with your order. We have initiated a replacement delivery.";
        }
        if (action == ActionType.COUPON) {
            return "Sorry about the issue with your order. We have issued a goodwill coupon for this experience.";
        }
        return "We're sorry about the issue with your order. Your case has been forwarded to a support specialist.";
    }
}
