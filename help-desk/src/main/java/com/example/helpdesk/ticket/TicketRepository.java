package com.example.helpdesk.ticket;

import org.springframework.data.jpa.repository.*;

public interface TicketRepository
    extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {}
