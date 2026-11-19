package com.example.helpdesk.ticket;

import com.example.helpdesk.auth.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Ticket {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 150)
  public String title;

  @Column(nullable = false, length = 4000)
  public String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Status status = Status.OPEN;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Priority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Category category;

  @ManyToOne(optional = false)
  public User customer;

  @ManyToOne public User agent;

  @Column(nullable = false)
  public Instant createdAt = Instant.now();

  @Column(nullable = false)
  public Instant updatedAt = Instant.now();

  @Version public Long version;
}
