package com.example.ZeptoSupportTicketManager.repositories;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewTicketRepository extends JpaRepository<NewTicket, Long> {
    boolean existsByOrderIdAndDescription(Long orderId, String description);
}
