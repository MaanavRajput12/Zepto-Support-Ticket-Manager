package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?:rs\\.?|inr|₹)?\\s*(\\d+(?:\\.\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE);

    public ActionResult execute(ActionType action, OrderContext order, List<SimilarTicketResponse> precedents) {
        if (action == ActionType.REFUND) {
            double amount = calculateSafeRefundAmount(order, precedents);
            return new ActionResult(true, action, "Simulated refund of Rs " + amount + " initiated", amount);
        }
        if (action == ActionType.REDELIVERY) {
            return new ActionResult(true, action, "Simulated replacement delivery initiated", null);
        }
        if (action == ActionType.COUPON) {
            return new ActionResult(true, action, "Simulated goodwill coupon issued", null);
        }
        return noAutomaticAction();
    }

    public ActionResult noAutomaticAction() {
        return new ActionResult(false, ActionType.NONE, "No automatic action taken; ticket requires human review",
                null);
    }

    public double calculateSafeRefundAmount(OrderContext order, List<SimilarTicketResponse> precedents) {
        double orderValue = order.getValue() == null ? 0.0 : Math.max(0.0, order.getValue());
        Optional<Double> precedentAmount = precedents.stream()
                .map(this::extractAmount)
                .flatMap(Optional::stream)
                .filter(amount -> amount > 0)
                .findFirst();
        double requestedAmount = precedentAmount.orElse(Math.min(orderValue, Math.max(10.0, orderValue * 0.25)));
        return round(Math.min(requestedAmount, orderValue));
    }

    private Optional<Double> extractAmount(SimilarTicketResponse precedent) {
        String text = ((precedent.getResolutionNote() == null ? "" : precedent.getResolutionNote()) + " "
                + (precedent.getDescription() == null ? "" : precedent.getDescription())).replace(",", "");
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1));
                if (amount > 0) {
                    return Optional.of(amount);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return Optional.empty();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
