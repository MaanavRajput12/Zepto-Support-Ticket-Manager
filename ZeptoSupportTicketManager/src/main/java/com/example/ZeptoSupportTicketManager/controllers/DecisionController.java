package com.example.ZeptoSupportTicketManager.controllers;

import com.example.ZeptoSupportTicketManager.responses.DecisionLogResponse;
import com.example.ZeptoSupportTicketManager.services.DecisionLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionLogService decisionLogService;

    public DecisionController(DecisionLogService decisionLogService) {
        this.decisionLogService = decisionLogService;
    }

    @GetMapping
    public List<DecisionLogResponse> findAll() {
        return decisionLogService.findAll();
    }

    @GetMapping("/{id}")
    public DecisionLogResponse findById(@PathVariable Long id) {
        return decisionLogService.findById(id);
    }
}
