package com.example.ZeptoSupportTicketManager.entities;

import com.example.ZeptoSupportTicketManager.enums.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resolved_tickets")
public class ResolvedTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionTaken;

    @Column(columnDefinition = "TEXT")
    private String resolutionNote;

    private Double csat;

    public ResolvedTicket() {
    }

    public ResolvedTicket(Long id, String description, ActionType actionTaken, String resolutionNote, Double csat) {
        this.id = id;
        this.description = description;
        this.actionTaken = actionTaken;
        this.resolutionNote = resolutionNote;
        this.csat = csat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionType getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(ActionType actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Double getCsat() {
        return csat;
    }

    public void setCsat(Double csat) {
        this.csat = csat;
    }
}
