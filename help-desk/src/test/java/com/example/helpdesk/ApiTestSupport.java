package com.example.helpdesk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestSupport {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @MockitoBean JavaMailSender mail;

  JsonNode body(ResultActions result) throws Exception {
    return json.readTree(result.andReturn().getResponse().getContentAsString());
  }

  ResultActions send(MockHttpServletRequestBuilder request, String token, String data)
      throws Exception {
    if (token != null) request.header("Authorization", "Bearer " + token);
    if (data != null) request.contentType("application/json").content(data);
    return mvc.perform(request);
  }

  String registerAndLogin() throws Exception {
    String email = UUID.randomUUID() + "@example.test";
    register(email, "Password123!");
    return login(email, "Password123!");
  }

  JsonNode register(String email, String password) throws Exception {
    return body(
        send(
                post("/api/auth/register"),
                null,
                json.writeValueAsString(
                    java.util.Map.of("name", "Student", "email", email, "password", password)))
            .andExpect(status().isCreated()));
  }

  String login(String email, String password) throws Exception {
    return body(send(
                post("/api/auth/login"),
                null,
                json.writeValueAsString(java.util.Map.of("email", email, "password", password)))
            .andExpect(status().isOk()))
        .get("accessToken")
        .asText();
  }
}
