package com.example.helpdesk.ticket;

import com.example.helpdesk.auth.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  private final TicketService service;
  private final CurrentUser users;

  public TicketController(TicketService service, CurrentUser users) {
    this.service = service;
    this.users = users;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Ticket create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TicketRequests.Details request) {
    return service.create(users.get(jwt), request);
  }

  @GetMapping
  public Page<Ticket> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Status status,
      @RequestParam(required = false) Priority priority,
      @RequestParam(required = false) Category category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String direction) {
    return service.search(
        users.get(jwt), q, status, priority, category, page, size, sortBy, direction);
  }

  @GetMapping("/{id}")
  public Ticket get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.get(users.get(jwt), id);
  }

  @PutMapping("/{id}")
  public Ticket update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody TicketRequests.Details request) {
    return service.update(users.get(jwt), id, request);
  }

  @PatchMapping("/{id}/assignment")
  public Ticket assign(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody TicketRequests.Assignment request) {
    return service.assign(users.get(jwt), id, request.agentId());
  }

  @PatchMapping("/{id}/status")
  public Ticket changeStatus(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody TicketRequests.StatusChange request) {
    return service.changeStatus(users.get(jwt), id, request.status());
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public Comment comment(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody TicketRequests.NewComment request) {
    return service.addComment(users.get(jwt), id, request.message());
  }

  @GetMapping("/{id}/comments")
  public List<Comment> comments(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.comments(users.get(jwt), id);
  }

  @GetMapping("/{id}/history")
  public List<TicketHistory> history(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.history(users.get(jwt), id);
  }
}
