package com.example.ZeptoSupportTicketManager.responses;

public class NewTicketResponse {

    private Long id;
    private String description;
    private Long orderId;

    public NewTicketResponse(Long id, String description, Long orderId) {
        this.id = id;
        this.description = description;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Long getOrderId() {
        return orderId;
    }
}
