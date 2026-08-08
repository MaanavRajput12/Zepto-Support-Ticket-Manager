package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.dto.CreateTicketRequest;
import com.example.ZeptoSupportTicketManager.dto.HumanReviewRequest;
import com.example.ZeptoSupportTicketManager.entities.DecisionLog;
import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.exceptions.NotFoundException;
import com.example.ZeptoSupportTicketManager.repositories.DecisionLogRepository;
import com.example.ZeptoSupportTicketManager.repositories.NewTicketRepository;
import com.example.ZeptoSupportTicketManager.repositories.OrderRepository;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import com.example.ZeptoSupportTicketManager.responses.NewTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.OrderContextResponse;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.TicketResolutionResponse;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketResolutionService {

    private final NewTicketRepository newTicketRepository;
    private final OrderRepository orderRepository;
    private final DecisionLogRepository decisionLogRepository;
    private final SimilarityService similarityService;
    private final DecisionEngine decisionEngine;
    private final ActionService actionService;
    private final ReplyGenerationService replyGenerationService;

    public TicketResolutionService(NewTicketRepository newTicketRepository, OrderRepository orderRepository,
            DecisionLogRepository decisionLogRepository, SimilarityService similarityService,
            DecisionEngine decisionEngine, ActionService actionService,
            ReplyGenerationService replyGenerationService) {
        this.newTicketRepository = newTicketRepository;
        this.orderRepository = orderRepository;
        this.decisionLogRepository = decisionLogRepository;
        this.similarityService = similarityService;
        this.decisionEngine = decisionEngine;
        this.actionService = actionService;
        this.replyGenerationService = replyGenerationService;
    }

    public List<NewTicketResponse> findAllTickets() {
        return newTicketRepository.findAll().stream().map(this::toTicketResponse).toList();
    }

    public NewTicketResponse findTicket(Long id) {
        return toTicketResponse(loadTicket(id));
    }

    @Transactional
    public NewTicketResponse createTicket(CreateTicketRequest request) {
        orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order Not Found",
                        "No order exists with id " + request.getOrderId()));
        NewTicket ticket = new NewTicket();
        ticket.setDescription(request.getDescription().trim());
        ticket.setOrderId(request.getOrderId());
        return toTicketResponse(newTicketRepository.save(ticket));
    }

    public List<SimilarTicketResponse> findPrecedents(Long ticketId) {
        NewTicket ticket = loadTicket(ticketId);
        return similarityService.findTopSimilarTickets(ticket.getDescription());
    }

    @Transactional
    public TicketResolutionResponse resolveTicket(Long ticketId) {
        NewTicket ticket = loadTicket(ticketId);
        OrderContext order = orderRepository.findById(ticket.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order Not Found",
                        "No order exists with id " + ticket.getOrderId()));
        List<SimilarTicketResponse> precedents = similarityService.findTopSimilarTickets(ticket.getDescription());
        DecisionResult decision = decisionEngine.decide(ticket, precedents, order);
        ActionResult actionResult = decision.getDecision() == DecisionType.AUTO_RESOLVED
                ? actionService.execute(decision.getSuggestedAction(), order, precedents)
                : actionService.noAutomaticAction();
        ActionType executedAction = actionResult.isSuccess() ? actionResult.getAction() : ActionType.NONE;
        String draftedReply = replyGenerationService.generateReply(ticket, order, decision, actionResult);
        DecisionLog log = saveDecisionLog(ticket, decision.getDecision(), decision.getConfidence(),
                decision.getSuggestedAction(), executedAction, actionResult, precedents, decision.getReasoning(),
                draftedReply, null);

        return toResolutionResponse(ticket, order, log, actionResult, precedents);
    }

    @Transactional
    public List<TicketResolutionResponse> resolveUnprocessedTickets() {
        return newTicketRepository.findAll().stream()
                .filter(ticket -> !decisionLogRepository.existsByTicketId(ticket.getId()))
                .map(ticket -> resolveTicket(ticket.getId()))
                .toList();
    }

    @Transactional
    public TicketResolutionResponse approveHumanReview(Long ticketId) {
        NewTicket ticket = loadTicket(ticketId);
        OrderContext order = loadOrder(ticket.getOrderId());
        DecisionLog latest = latestDecision(ticketId);
        ActionType suggestedAction = latest.getSuggestedAction();
        validateExecutableAction(suggestedAction, order);
        List<SimilarTicketResponse> precedents = similarityService.findTopSimilarTickets(ticket.getDescription());
        ActionResult actionResult = actionService.execute(suggestedAction, order, precedents);
        String reasoning = "Human approved the AI-suggested action " + suggestedAction
                + ". Backend safety checks passed before execution.";
        String draftedReply = humanResolvedReply(suggestedAction, actionResult);
        DecisionLog log = saveDecisionLog(ticket, DecisionType.HUMAN_REVIEW, latest.getConfidence(),
                suggestedAction, actionResult.getAction(), actionResult, precedents, reasoning, draftedReply,
                "Approved suggested action");
        return toResolutionResponse(ticket, order, log, actionResult, precedents);
    }

    @Transactional
    public TicketResolutionResponse overrideHumanReview(Long ticketId, HumanReviewRequest request) {
        NewTicket ticket = loadTicket(ticketId);
        OrderContext order = loadOrder(ticket.getOrderId());
        latestDecision(ticketId);
        ActionType overrideAction = request.getAction();
        validateExecutableAction(overrideAction, order);
        List<SimilarTicketResponse> precedents = similarityService.findTopSimilarTickets(ticket.getDescription());
        ActionResult actionResult = actionService.execute(overrideAction, order, precedents);
        String note = request.getReviewNote() == null || request.getReviewNote().isBlank()
                ? "No reviewer note provided"
                : request.getReviewNote().trim();
        String reasoning = "Human reviewer overrode the recommendation and executed " + overrideAction
                + ". Reviewer note: " + note + ". Backend safety checks passed before execution.";
        String draftedReply = humanResolvedReply(overrideAction, actionResult);
        DecisionLog log = saveDecisionLog(ticket, DecisionType.HUMAN_REVIEW, null, overrideAction,
                actionResult.getAction(), actionResult, precedents, reasoning, draftedReply, note);
        return toResolutionResponse(ticket, order, log, actionResult, precedents);
    }

    private NewTicket loadTicket(Long id) {
        return newTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket Not Found", "No ticket exists with id " + id));
    }

    private OrderContext loadOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order Not Found", "No order exists with id " + id));
    }

    private DecisionLog latestDecision(Long ticketId) {
        return decisionLogRepository.findTopByTicketIdOrderByCreatedAtDesc(ticketId)
                .orElseThrow(() -> new NotFoundException("Decision Not Found",
                        "Run the AI decision before approving or overriding ticket " + ticketId));
    }

    private void validateExecutableAction(ActionType action, OrderContext order) {
        if (!EnumSet.of(ActionType.REFUND, ActionType.REDELIVERY, ActionType.COUPON).contains(action)) {
            throw new IllegalArgumentException("No executable support action was selected");
        }
        if (!decisionEngine.isActionCompatible(action, order)) {
            throw new IllegalArgumentException(action + " is not safe for order " + order.getId()
                    + " with status " + order.getStatus());
        }
    }

    private DecisionLog saveDecisionLog(NewTicket ticket, DecisionType decision, Double confidence,
            ActionType suggestedAction, ActionType executedAction, ActionResult actionResult,
            List<SimilarTicketResponse> precedents, String reasoning, String draftedReply, String reviewNote) {
        DecisionLog log = new DecisionLog();
        log.setTicketId(ticket.getId());
        log.setConfidence(confidence == null ? 100.0 : confidence);
        log.setDecision(decision);
        log.setSuggestedAction(suggestedAction == null ? ActionType.NONE : suggestedAction);
        log.setExecutedAction(executedAction == null ? ActionType.NONE : executedAction);
        log.setActionMessage(actionResult == null ? null : actionResult.getMessage());
        log.setActionAmount(actionResult == null ? null : actionResult.getAmount());
        log.setPrecedentIds(precedents.stream()
                .map(precedent -> String.valueOf(precedent.getTicketId()))
                .collect(Collectors.joining(",")));
        log.setReviewNote(reviewNote);
        log.setReasoning(reasoning);
        log.setDraftedReply(draftedReply);
        return decisionLogRepository.save(log);
    }

    private TicketResolutionResponse toResolutionResponse(NewTicket ticket, OrderContext order, DecisionLog log,
            ActionResult actionResult, List<SimilarTicketResponse> precedents) {
        return new TicketResolutionResponse(ticket.getId(), ticket.getDescription(), ticket.getOrderId(),
                toOrderResponse(order), log.getDecision(), log.getConfidence(), log.getSuggestedAction(),
                log.getExecutedAction(), actionResult, precedents, log.getReasoning(), log.getDraftedReply());
    }

    private OrderContextResponse toOrderResponse(OrderContext order) {
        return new OrderContextResponse(order.getId(), order.getItems(), order.getValue(), order.getDeliveryTime(),
                order.getStatus());
    }

    private String humanResolvedReply(ActionType action, ActionResult actionResult) {
        if (action == ActionType.REFUND) {
            return "Sorry about the issue with your order. A support specialist reviewed your case and initiated a refund of Rs "
                    + actionResult.getAmount() + ".";
        }
        if (action == ActionType.REDELIVERY) {
            return "Sorry about the issue with your order. A support specialist reviewed your case and initiated a replacement delivery.";
        }
        if (action == ActionType.COUPON) {
            return "Sorry about the issue with your order. A support specialist reviewed your case and issued a goodwill coupon.";
        }
        return "We're sorry about the issue with your order. Your case has been reviewed by our support team.";
    }

    private NewTicketResponse toTicketResponse(NewTicket ticket) {
        OrderContextResponse order = orderRepository.findById(ticket.getOrderId())
                .map(this::toOrderResponse)
                .orElse(null);
        return new NewTicketResponse(ticket.getId(), ticket.getDescription(), ticket.getOrderId(), order);
    }
}
