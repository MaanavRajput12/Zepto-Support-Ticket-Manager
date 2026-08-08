package com.example.ZeptoSupportTicketManager.responses;

public class NewTicketResponse {

    private Long id;
    private String description;
    private Long orderId;
    private OrderContextResponse order;

    public NewTicketResponse(Long id, String description, Long orderId, OrderContextResponse order) {
        this.id = id;
        this.description = description;
        this.orderId = orderId;
        this.order = order;
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

    public OrderContextResponse getOrder() {
        return order;
    }
}
