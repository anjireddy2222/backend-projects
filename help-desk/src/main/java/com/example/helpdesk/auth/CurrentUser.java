package com.example.helpdesk.auth;

import com.example.helpdesk.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
  private final UserRepository users;

  public CurrentUser(UserRepository users) {
    this.users = users;
  }

  public User get(Jwt jwt) {
    try {
      return users
          .findById(Long.parseLong(jwt.getSubject()))
          .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    } catch (NumberFormatException ex) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid account token");
    }
  }
}
