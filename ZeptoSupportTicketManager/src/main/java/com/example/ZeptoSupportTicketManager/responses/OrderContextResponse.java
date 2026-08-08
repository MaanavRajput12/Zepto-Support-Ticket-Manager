package com.example.ZeptoSupportTicketManager.responses;

public class OrderContextResponse {

    private Long id;
    private String items;
    private Double value;
    private String deliveryTime;
    private String status;

    public OrderContextResponse(Long id, String items, Double value, String deliveryTime, String status) {
        this.id = id;
        this.items = items;
        this.value = value;
        this.deliveryTime = deliveryTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getItems() {
        return items;
    }

    public Double getValue() {
        return value;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getStatus() {
        return status;
    }
}
