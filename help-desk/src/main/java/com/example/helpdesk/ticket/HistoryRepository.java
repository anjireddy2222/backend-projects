package com.example.helpdesk.ticket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<TicketHistory, Long> {
  List<TicketHistory> findByTicketIdOrderByIdAsc(Long ticketId);
}
