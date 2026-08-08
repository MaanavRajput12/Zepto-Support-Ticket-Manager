package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.dto.CreateTicketRequest;
import com.example.ZeptoSupportTicketManager.entities.DecisionLog;
import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.enums.DecisionType;
import com.example.ZeptoSupportTicketManager.exceptions.NotFoundException;
import com.example.ZeptoSupportTicketManager.repositories.DecisionLogRepository;
import com.example.ZeptoSupportTicketManager.repositories.NewTicketRepository;
import com.example.ZeptoSupportTicketManager.repositories.OrderRepository;
import com.example.ZeptoSupportTicketManager.responses.ActionResult;
import com.example.ZeptoSupportTicketManager.responses.NewTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.TicketResolutionResponse;
import java.util.List;
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
                ? actionService.execute(decision.getSelectedAction(), order, precedents)
                : actionService.noAutomaticAction();
        String draftedReply = replyGenerationService.generateReply(ticket, order, decision, actionResult);

        DecisionLog log = new DecisionLog();
        log.setTicketId(ticket.getId());
        log.setConfidence(decision.getConfidence());
        log.setDecision(decision.getDecision());
        log.setSelectedAction(decision.getSelectedAction());
        log.setReasoning(decision.getReasoning());
        log.setDraftedReply(draftedReply);
        decisionLogRepository.save(log);

        return new TicketResolutionResponse(ticket.getId(), ticket.getDescription(), decision.getDecision(),
                decision.getConfidence(), decision.getSelectedAction(), actionResult, precedents,
                decision.getReasoning(), draftedReply);
    }

    private NewTicket loadTicket(Long id) {
        return newTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket Not Found", "No ticket exists with id " + id));
    }

    private NewTicketResponse toTicketResponse(NewTicket ticket) {
        return new NewTicketResponse(ticket.getId(), ticket.getDescription(), ticket.getOrderId());
    }
}
