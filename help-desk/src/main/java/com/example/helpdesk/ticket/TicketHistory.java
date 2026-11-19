package com.example.helpdesk.ticket;

import com.example.helpdesk.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class TicketHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JsonIgnore
  public Ticket ticket;

  @ManyToOne(optional = false)
  public User actor;

  @Column(nullable = false, length = 200)
  public String action;

  @Enumerated(EnumType.STRING)
  public Status fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Status toStatus;

  @Column(nullable = false)
  public Instant createdAt = Instant.now();
}
