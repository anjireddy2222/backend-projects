package com.example.helpdesk.ticket;

import com.example.helpdesk.auth.*;
import com.example.helpdesk.common.ApiException;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TicketService {
  private final TicketRepository tickets;
  private final UserRepository users;
  private final CommentRepository comments;
  private final HistoryRepository history;

  public TicketService(
      TicketRepository tickets,
      UserRepository users,
      CommentRepository comments,
      HistoryRepository history) {
    this.tickets = tickets;
    this.users = users;
    this.comments = comments;
    this.history = history;
  }

  public Ticket create(User user, TicketRequests.Details request) {
    if (user.role.name != Role.CUSTOMER)
      throw new ApiException(HttpStatus.FORBIDDEN, "Only customers can create tickets");
    Ticket ticket = new Ticket();
    ticket.customer = user;
    applyDetails(ticket, request);
    tickets.save(ticket);
    record(ticket, user, "Ticket created", null);
    return ticket;
  }

  public Ticket get(User user, Long id) {
    Ticket ticket =
        tickets.findById(id).orElseThrow(() -> ApiException.notFound("Ticket not found"));
    boolean allowed =
        user.role.name == Role.ADMIN
            || (user.role.name == Role.CUSTOMER && ticket.customer.id.equals(user.id))
            || (user.role.name == Role.AGENT
                && ticket.agent != null
                && ticket.agent.id.equals(user.id));
    if (!allowed) throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this ticket");
    return ticket;
  }

  public Ticket update(User user, Long id, TicketRequests.Details request) {
    Ticket ticket = get(user, id);
    if (ticket.status == Status.CLOSED || ticket.status == Status.RESOLVED)
      throw ApiException.badRequest("Resolved or closed tickets cannot be edited");
    if (user.role.name == Role.CUSTOMER && ticket.status != Status.OPEN)
      throw ApiException.badRequest("Customers may edit only open tickets");
    applyDetails(ticket, request);
    record(ticket, user, "Details updated", ticket.status);
    return ticket;
  }

  public Ticket assign(User user, Long id, Long agentId) {
    requireAdmin(user);
    Ticket ticket = get(user, id);
    if (ticket.status == Status.RESOLVED || ticket.status == Status.CLOSED)
      throw ApiException.badRequest("Only active tickets can be assigned");
    User agent =
        users.findById(agentId).orElseThrow(() -> ApiException.notFound("Agent not found"));
    if (agent.role.name != Role.AGENT)
      throw ApiException.badRequest("Selected user must have the AGENT role");
    ticket.agent = agent;
    ticket.updatedAt = Instant.now();
    record(ticket, user, "Assigned to agent #" + agent.id, ticket.status);
    return ticket;
  }

  public Ticket changeStatus(User user, Long id, Status next) {
    Ticket ticket = get(user, id);
    Status previous = ticket.status;
    boolean valid =
        (previous == Status.OPEN && next == Status.IN_PROGRESS)
            || (previous == Status.IN_PROGRESS && next == Status.RESOLVED)
            || (previous == Status.RESOLVED && next == Status.CLOSED);
    if (!valid)
      throw ApiException.badRequest("Allowed workflow: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED");
    if (user.role.name == Role.CUSTOMER && next != Status.CLOSED)
      throw new ApiException(HttpStatus.FORBIDDEN, "Customers can only close resolved tickets");
    if (next == Status.IN_PROGRESS && ticket.agent == null)
      throw ApiException.badRequest("Assign an agent before starting work");
    ticket.status = next;
    ticket.updatedAt = Instant.now();
    record(ticket, user, "Status changed", previous);
    return ticket;
  }

  public Comment addComment(User user, Long id, String message) {
    Ticket ticket = get(user, id);
    if (ticket.status == Status.CLOSED)
      throw ApiException.badRequest("Closed tickets cannot receive comments");
    Comment comment = new Comment();
    comment.ticket = ticket;
    comment.author = user;
    comment.message = message.trim();
    ticket.updatedAt = Instant.now();
    return comments.save(comment);
  }

  public List<Comment> comments(User user, Long id) {
    get(user, id);
    return comments.findByTicketIdOrderByIdAsc(id);
  }

  public List<TicketHistory> history(User user, Long id) {
    get(user, id);
    return history.findByTicketIdOrderByIdAsc(id);
  }

  public Page<Ticket> search(
      User user,
      String q,
      Status status,
      Priority priority,
      Category category,
      int page,
      int size,
      String sortBy,
      String direction) {
    if (page < 0 || size < 1 || size > 100)
      throw ApiException.badRequest("page must be >= 0 and size between 1 and 100");
    if (!List.of("id", "title", "createdAt", "updatedAt").contains(sortBy))
      throw ApiException.badRequest("sortBy must be id, title, createdAt or updatedAt");
    if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))
      throw ApiException.badRequest("direction must be asc or desc");
    Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy).and(Sort.by("id"));
    // Apply visibility inside the database query, before pagination.
    return tickets.findAll(
        (root, query, cb) -> {
          List<Predicate> filters = new ArrayList<>();
          if (user.role.name == Role.CUSTOMER)
            filters.add(cb.equal(root.get("customer").get("id"), user.id));
          if (user.role.name == Role.AGENT)
            filters.add(cb.equal(root.get("agent").get("id"), user.id));
          if (status != null) filters.add(cb.equal(root.get("status"), status));
          if (priority != null) filters.add(cb.equal(root.get("priority"), priority));
          if (category != null) filters.add(cb.equal(root.get("category"), category));
          String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
          filters.add(
              cb.or(
                  cb.like(cb.lower(root.get("title")), pattern),
                  cb.like(cb.lower(root.get("description")), pattern)));
          return cb.and(filters.toArray(new Predicate[0]));
        },
        PageRequest.of(page, size, sort));
  }

  public List<User> agents(User user) {
    requireAdmin(user);
    return users.findByRoleName(Role.AGENT);
  }

  private void requireAdmin(User user) {
    if (user.role.name != Role.ADMIN)
      throw new ApiException(HttpStatus.FORBIDDEN, "Administrator role required");
  }

  private void applyDetails(Ticket ticket, TicketRequests.Details request) {
    ticket.title = request.title().trim();
    ticket.description = request.description().trim();
    ticket.priority = request.priority();
    ticket.category = request.category();
    ticket.updatedAt = Instant.now();
  }

  private void record(Ticket ticket, User user, String action, Status previous) {
    TicketHistory entry = new TicketHistory();
    entry.ticket = ticket;
    entry.actor = user;
    entry.action = action;
    entry.fromStatus = previous;
    entry.toStatus = ticket.status;
    history.save(entry);
  }
}
