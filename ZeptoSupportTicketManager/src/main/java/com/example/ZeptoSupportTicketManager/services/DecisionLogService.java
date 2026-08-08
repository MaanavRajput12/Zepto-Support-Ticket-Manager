package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.entities.DecisionLog;
import com.example.ZeptoSupportTicketManager.exceptions.NotFoundException;
import com.example.ZeptoSupportTicketManager.repositories.DecisionLogRepository;
import com.example.ZeptoSupportTicketManager.responses.DecisionLogResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecisionLogService {

    private final DecisionLogRepository decisionLogRepository;

    public DecisionLogService(DecisionLogRepository decisionLogRepository) {
        this.decisionLogRepository = decisionLogRepository;
    }

    public List<DecisionLogResponse> findAll() {
        return decisionLogRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DecisionLogResponse findById(Long id) {
        return decisionLogRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Decision Log Not Found",
                        "No decision log exists with id " + id));
    }

    private DecisionLogResponse toResponse(DecisionLog log) {
        return new DecisionLogResponse(log.getId(), log.getTicketId(), log.getConfidence(), log.getDecision(),
                log.getSuggestedAction(), log.getExecutedAction(), log.getActionMessage(), log.getActionAmount(),
                log.getPrecedentIds(), log.getReviewNote(), log.getReasoning(), log.getDraftedReply(),
                log.getCreatedAt());
    }
}
