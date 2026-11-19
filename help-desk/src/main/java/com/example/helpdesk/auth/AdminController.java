package com.example.helpdesk.auth;

import com.example.helpdesk.ticket.TicketService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final TicketService tickets;
  private final CurrentUser users;

  public AdminController(TicketService tickets, CurrentUser users) {
    this.tickets = tickets;
    this.users = users;
  }

  @GetMapping("/agents")
  public List<User> agents(@AuthenticationPrincipal Jwt jwt) {
    return tickets.agents(users.get(jwt));
  }
}
