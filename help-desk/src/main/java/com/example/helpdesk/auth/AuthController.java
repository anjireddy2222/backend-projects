package com.example.helpdesk.auth;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  private final CurrentUser currentUser;

  public AuthController(AuthService service, CurrentUser currentUser) {
    this.service = service;
    this.currentUser = currentUser;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public User register(@Valid @RequestBody AuthRequests.Register request) {
    return service.register(request);
  }

  @PostMapping("/login")
  public AuthRequests.Token login(@Valid @RequestBody AuthRequests.Login request) {
    return service.login(request);
  }

  @PostMapping("/forgot-password")
  public Map<String, String> forgot(@Valid @RequestBody AuthRequests.Forgot request) {
    service.forgot(request.email());
    return Map.of("message", "If the account exists, a reset token has been emailed");
  }

  @PostMapping("/reset-password")
  public Map<String, String> reset(@Valid @RequestBody AuthRequests.Reset request) {
    service.reset(request);
    return Map.of("message", "Password changed. Log in with the new password");
  }

  @GetMapping("/me")
  public User me(@AuthenticationPrincipal Jwt jwt) {
    return currentUser.get(jwt);
  }
}
