package com.example.amazon.customer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.amazon.customer.auth.PasswordResetRepository;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

class AuthIntegrationTest extends ApiTestSupport {
  @Autowired JwtEncoder encoder;
  @Autowired PasswordResetRepository resets;

  @Test
  void registrationLoginAndAuthentication() throws Exception {
    String email = UUID.randomUUID() + "@example.test";
    var user = register(email, "Password123!");
    assertThat(user.has("password")).isFalse();
    assertThat(user.get("role").asText()).isEqualTo("CUSTOMER");
    send(
            post("/api/auth/register"),
            null,
            json.writeValueAsString(
                Map.of(
                    "name",
                    "Other",
                    "email",
                    email.toUpperCase(Locale.ROOT),
                    "password",
                    "Password123!")))
        .andExpect(status().isConflict());
    send(
            post("/api/auth/login"),
            null,
            json.writeValueAsString(Map.of("email", email, "password", "incorrect")))
        .andExpect(status().isUnauthorized());
    String token = login(email, "Password123!");
    send(get("/api/auth/me"), token, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("email").value(email));
    send(get("/api/auth/me"), null, null).andExpect(status().isUnauthorized());
    send(get("/api/auth/me"), token + "invalid", null).andExpect(status().isUnauthorized());
    send(
            post("/api/auth/register"),
            null,
            "{\"name\":\"\",\"email\":\"wrong\",\"password\":\"123\"}")
        .andExpect(status().isBadRequest());
  }

  @Test
  void oversizedUnicodePasswordReturnsValidationError() throws Exception {
    send(
            post("/api/auth/register"),
            null,
            json.writeValueAsString(
                Map.of(
                    "name",
                    "Student",
                    "email",
                    "unicode@example.test",
                    "password",
                    "\u00e9".repeat(40))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void expiredJwtIsRejected() throws Exception {
    var claims =
        JwtClaimsSet.builder()
            .issuer("amazon_customer")
            .subject("1")
            .issuedAt(Instant.now().minusSeconds(7200))
            .expiresAt(Instant.now().minusSeconds(3600))
            .build();
    String expired =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    send(get("/api/auth/me"), expired, null).andExpect(status().isUnauthorized());
  }

  @Test
  void passwordResetIsEmailedAndSingleUse() throws Exception {
    String email = UUID.randomUUID() + "@example.test";
    register(email, "Password123!");
    String forgot = json.writeValueAsString(Map.of("email", email));
    var existing =
        body(send(post("/api/auth/forgot-password"), null, forgot).andExpect(status().isOk()));
    var unknown =
        body(
            send(post("/api/auth/forgot-password"), null, "{\"email\":\"unknown@example.test\"}")
                .andExpect(status().isOk()));
    assertThat(existing).isEqualTo(unknown);
    ArgumentCaptor<SimpleMailMessage> capture = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mail).send(capture.capture());
    String rawToken = capture.getValue().getText().split("\\n")[1];
    assertThat(resets.findAll()).noneMatch(r -> r.tokenHash.equals(rawToken));
    String reset = json.writeValueAsString(Map.of("token", rawToken, "password", "Changed123!"));
    send(post("/api/auth/reset-password"), null, reset).andExpect(status().isOk());
    send(post("/api/auth/reset-password"), null, reset).andExpect(status().isBadRequest());
    login(email, "Changed123!");
    send(
            post("/api/auth/login"),
            null,
            json.writeValueAsString(Map.of("email", email, "password", "Password123!")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void expiredResetCannotChangePassword() throws Exception {
    String email = UUID.randomUUID() + "@example.test";
    register(email, "Password123!");
    send(post("/api/auth/forgot-password"), null, json.writeValueAsString(Map.of("email", email)))
        .andExpect(status().isOk());
    var capture = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mail).send(capture.capture());
    String token = capture.getValue().getText().split("\\n")[1];
    var reset =
        resets.findAll().stream().filter(r -> r.user.email.equals(email)).findFirst().orElseThrow();
    reset.expiresAt = Instant.now().minusSeconds(1);
    resets.save(reset);
    send(
            post("/api/auth/reset-password"),
            null,
            json.writeValueAsString(Map.of("token", token, "password", "Changed123!")))
        .andExpect(status().isBadRequest());
    login(email, "Password123!");
  }
}
