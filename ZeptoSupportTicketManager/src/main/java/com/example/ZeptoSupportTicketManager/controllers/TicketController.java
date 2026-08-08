package com.example.ZeptoSupportTicketManager.controllers;

import com.example.ZeptoSupportTicketManager.dto.CreateTicketRequest;
import com.example.ZeptoSupportTicketManager.responses.NewTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import com.example.ZeptoSupportTicketManager.responses.TicketResolutionResponse;
import com.example.ZeptoSupportTicketManager.services.TicketResolutionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketResolutionService ticketResolutionService;

    public TicketController(TicketResolutionService ticketResolutionService) {
        this.ticketResolutionService = ticketResolutionService;
    }

    @GetMapping
    public List<NewTicketResponse> findAllTickets() {
        return ticketResolutionService.findAllTickets();
    }

    @GetMapping("/{id}")
    public NewTicketResponse findTicket(@PathVariable Long id) {
        return ticketResolutionService.findTicket(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewTicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketResolutionService.createTicket(request);
    }

    @GetMapping("/{id}/precedents")
    public List<SimilarTicketResponse> findPrecedents(@PathVariable Long id) {
        return ticketResolutionService.findPrecedents(id);
    }

    @PostMapping("/{id}/resolve")
    public TicketResolutionResponse resolveTicket(@PathVariable Long id) {
        return ticketResolutionService.resolveTicket(id);
    }
}
