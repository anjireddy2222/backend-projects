package com.example.helpdesk.auth;

import com.example.helpdesk.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
  private final UserRepository users;
  private final PasswordResetRepository resets;
  private final PasswordEncoder passwords;
  private final JwtEncoder tokens;
  private final JavaMailSender mail;
  private final String issuer;
  private final String from;

  public AuthService(
      UserRepository users,
      PasswordResetRepository resets,
      PasswordEncoder passwords,
      JwtEncoder tokens,
      JavaMailSender mail,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.mail.from}") String from) {
    this.users = users;
    this.resets = resets;
    this.passwords = passwords;
    this.tokens = tokens;
    this.mail = mail;
    this.issuer = issuer;
    this.from = from;
  }

  public User register(AuthRequests.Register request) {
    checkPasswordLength(request.password());
    String email = normalize(request.email());
    if (users.findByEmail(email).isPresent())
      throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
    User user = new User();
    user.name = request.name().trim();
    user.email = email;
    user.password = passwords.encode(request.password());
    // Public registration always creates a customer, never an administrator.
    return users.save(user);
  }

  public AuthRequests.Token login(AuthRequests.Login request) {
    User user =
        users
            .findByEmail(normalize(request.email()))
            .orElseThrow(
                () -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    if (!passwords.matches(request.password(), user.password))
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(user.id.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    return new AuthRequests.Token(
        tokens.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), "Bearer", 3600);
  }

  public void forgot(String email) {
    users
        .findByEmail(normalize(email))
        .ifPresent(
            user -> {
              resets.deleteByUserId(user.id);
              String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
              PasswordReset reset = new PasswordReset();
              reset.user = user;
              reset.tokenHash = hash(rawToken);
              reset.expiresAt = Instant.now().plusSeconds(900);
              resets.save(reset);
              SimpleMailMessage message = new SimpleMailMessage();
              message.setFrom(from);
              message.setTo(user.email);
              message.setSubject("Reset your password");
              message.setText(
                  "Use this one-time token with POST /api/auth/reset-password within 15 minutes:\n"
                      + rawToken);
              mail.send(message);
            });
  }

  public void reset(AuthRequests.Reset request) {
    checkPasswordLength(request.password());
    PasswordReset reset =
        resets
            .findByTokenHash(hash(request.token()))
            .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset token"));
    if (!reset.expiresAt.isAfter(Instant.now()))
      throw ApiException.badRequest("Invalid or expired reset token");
    reset.user.password = passwords.encode(request.password());
    users.save(reset.user);
    resets.deleteByUserId(reset.user.id);
  }

  private void checkPasswordLength(String password) {
    // BCrypt accepts at most 72 bytes; non-ASCII characters may use several bytes.
    if (password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw ApiException.badRequest("Password must be at most 72 UTF-8 bytes");
  }

  private String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String hash(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
