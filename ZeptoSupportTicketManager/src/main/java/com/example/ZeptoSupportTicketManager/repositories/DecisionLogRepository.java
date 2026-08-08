package com.example.ZeptoSupportTicketManager.repositories;

import com.example.ZeptoSupportTicketManager.entities.DecisionLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionLogRepository extends JpaRepository<DecisionLog, Long> {
    Optional<DecisionLog> findTopByTicketIdOrderByCreatedAtDesc(Long ticketId);

    boolean existsByTicketId(Long ticketId);
}
