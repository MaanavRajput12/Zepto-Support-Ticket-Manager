package com.example.ZeptoSupportTicketManager.repositories;

import com.example.ZeptoSupportTicketManager.entities.ResolvedTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResolvedTicketRepository extends JpaRepository<ResolvedTicket, Long> {
}
