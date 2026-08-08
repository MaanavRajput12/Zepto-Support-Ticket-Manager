package com.example.ZeptoSupportTicketManager.repositories;

import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderContext, Long> {
}
