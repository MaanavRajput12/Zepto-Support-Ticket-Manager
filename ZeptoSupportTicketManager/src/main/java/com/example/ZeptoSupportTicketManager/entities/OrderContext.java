package com.example.ZeptoSupportTicketManager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders_context")
public class OrderContext {

    @Id
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String items;

    @Column(name = "order_value", nullable = false)
    private Double value;

    private String deliveryTime;

    @Column(nullable = false)
    private String status;

    public OrderContext() {
    }

    public OrderContext(Long id, String items, Double value, String deliveryTime, String status) {
        this.id = id;
        this.items = items;
        this.value = value;
        this.deliveryTime = deliveryTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
