package com.example.helpdesk;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.helpdesk.auth.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TicketIntegrationTest extends ApiTestSupport {
  @Autowired UserRepository users;
  private final String details =
      """
{"title":"Cannot log in","description":"My account login fails","priority":"HIGH","category":"ACCOUNT"}
""";

  private String roleToken(Role role) throws Exception {
    String email = UUID.randomUUID() + "@example.test";
    var registered = register(email, "Password123!");
    User user = users.findById(registered.get("id").asLong()).orElseThrow();
    user.role = new RoleEntity(role);
    users.save(user);
    return login(email, "Password123!");
  }

  @Test
  void assignmentWorkflowCommentsAndHistory() throws Exception {
    String customer = registerAndLogin(),
        admin = roleToken(Role.ADMIN),
        agent = roleToken(Role.AGENT);
    long agentId = body(send(get("/api/auth/me"), agent, null)).get("id").asLong();
    long id =
        body(send(post("/api/tickets"), customer, details).andExpect(status().isCreated()))
            .get("id")
            .asLong();
    send(patch("/api/tickets/" + id + "/status"), admin, "{\"status\":\"IN_PROGRESS\"}")
        .andExpect(status().isBadRequest());
    send(patch("/api/tickets/" + id + "/assignment"), customer, "{\"agentId\":" + agentId + "}")
        .andExpect(status().isForbidden());
    send(patch("/api/tickets/" + id + "/assignment"), admin, "{\"agentId\":" + agentId + "}")
        .andExpect(status().isOk());
    send(patch("/api/tickets/" + id + "/status"), customer, "{\"status\":\"IN_PROGRESS\"}")
        .andExpect(status().isForbidden());
    send(patch("/api/tickets/" + id + "/status"), agent, "{\"status\":\"RESOLVED\"}")
        .andExpect(status().isBadRequest());
    send(patch("/api/tickets/" + id + "/status"), agent, "{\"status\":\"IN_PROGRESS\"}")
        .andExpect(status().isOk());
    send(put("/api/tickets/" + id), customer, details).andExpect(status().isBadRequest());
    send(post("/api/tickets/" + id + "/comments"), agent, "{\"message\":\"Investigating now\"}")
        .andExpect(status().isCreated());
    send(get("/api/tickets/" + id + "/comments"), customer, null)
        .andExpect(jsonPath("length()").value(1));
    send(patch("/api/tickets/" + id + "/status"), agent, "{\"status\":\"RESOLVED\"}")
        .andExpect(status().isOk());
    send(patch("/api/tickets/" + id + "/status"), customer, "{\"status\":\"CLOSED\"}")
        .andExpect(status().isOk());
    var history =
        body(
            send(get("/api/tickets/" + id + "/history"), customer, null)
                .andExpect(status().isOk()));
    assertThat(history.size()).isEqualTo(5);
    assertThat(history.get(4).get("fromStatus").asText()).isEqualTo("RESOLVED");
    assertThat(history.get(4).get("toStatus").asText()).isEqualTo("CLOSED");
    send(post("/api/tickets/" + id + "/comments"), customer, "{\"message\":\"Too late\"}")
        .andExpect(status().isBadRequest());
    send(put("/api/tickets/" + id), admin, details).andExpect(status().isBadRequest());
  }

  @Test
  void visibilitySearchFiltersAndPagination() throws Exception {
    String customer = registerAndLogin(),
        stranger = registerAndLogin(),
        agent = roleToken(Role.AGENT),
        admin = roleToken(Role.ADMIN);
    long id = body(send(post("/api/tickets"), customer, details)).get("id").asLong();
    send(
        post("/api/tickets"),
        customer,
        details
            .replace("Cannot log in", "Payment error")
            .replace("HIGH", "LOW")
            .replace("ACCOUNT", "BILLING"));
    for (String token : new String[] {stranger, agent}) {
      send(get("/api/tickets/" + id), token, null).andExpect(status().isForbidden());
      send(get("/api/tickets/" + id + "/comments"), token, null).andExpect(status().isForbidden());
      send(get("/api/tickets/" + id + "/history"), token, null).andExpect(status().isForbidden());
      send(put("/api/tickets/" + id), token, details).andExpect(status().isForbidden());
      send(get("/api/tickets"), token, null).andExpect(jsonPath("totalElements").value(0));
      send(get("/api/admin/agents"), token, null).andExpect(status().isForbidden());
    }
    send(
            get("/api/tickets")
                .param("q", "LOGIN")
                .param("priority", "HIGH")
                .param("category", "ACCOUNT")
                .param("status", "OPEN"),
            customer,
            null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("totalElements").value(1));
    send(
            get("/api/tickets")
                .param("sortBy", "title")
                .param("direction", "asc")
                .param("size", "1")
                .param("page", "1"),
            customer,
            null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("content[0].title").value("Payment error"))
        .andExpect(jsonPath("totalElements").value(2));
    send(get("/api/tickets/" + id), admin, null).andExpect(status().isOk());
    send(get("/api/admin/agents"), admin, null).andExpect(status().isOk());
  }

  @Test
  void validationAndNonAgentAssignment() throws Exception {
    String customer = registerAndLogin(), admin = roleToken(Role.ADMIN);
    send(post("/api/tickets"), admin, details).andExpect(status().isForbidden());
    long customerId = body(send(get("/api/auth/me"), customer, null)).get("id").asLong();
    long id = body(send(post("/api/tickets"), customer, details)).get("id").asLong();
    send(patch("/api/tickets/" + id + "/assignment"), admin, "{\"agentId\":" + customerId + "}")
        .andExpect(status().isBadRequest());
    send(post("/api/tickets"), customer, "{}").andExpect(status().isBadRequest());
    send(post("/api/tickets"), customer, details.replace("HIGH", "INVALID"))
        .andExpect(status().isBadRequest());
    send(get("/api/tickets").param("sortBy", "password"), customer, null)
        .andExpect(status().isBadRequest());
    send(get("/api/tickets").param("page", "-1"), customer, null)
        .andExpect(status().isBadRequest());
    send(get("/api/tickets").param("size", "101"), customer, null)
        .andExpect(status().isBadRequest());
    send(get("/api/tickets").param("direction", "sideways"), customer, null)
        .andExpect(status().isBadRequest());
    send(get("/api/tickets/999999"), customer, null).andExpect(status().isNotFound());
    send(put("/api/tickets/" + id), customer, details.replace("Cannot log in", "Updated title"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("title").value("Updated title"));
  }
}
