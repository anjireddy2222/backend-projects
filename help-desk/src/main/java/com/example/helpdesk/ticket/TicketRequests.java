package com.example.helpdesk.ticket;

import jakarta.validation.constraints.*;

public class TicketRequests {
  public record Details(
      @NotBlank @Size(max = 150) String title,
      @NotBlank @Size(max = 4000) String description,
      @NotNull Priority priority,
      @NotNull Category category) {}

  public record Assignment(@NotNull @Positive Long agentId) {}

  public record StatusChange(@NotNull Status status) {}

  public record NewComment(@NotBlank @Size(max = 2000) String message) {}
}
