package com.example.helpdesk.ticket;

import com.example.helpdesk.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JsonIgnore
  public Ticket ticket;

  @ManyToOne(optional = false)
  public User author;

  @Column(nullable = false, length = 2000)
  public String message;

  @Column(nullable = false)
  public Instant createdAt = Instant.now();
}
