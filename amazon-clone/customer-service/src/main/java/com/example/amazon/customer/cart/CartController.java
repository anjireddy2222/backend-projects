package com.example.amazon.customer.cart;

import com.example.amazon.customer.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private final CartService service;
  private final CurrentUser users;

  public CartController(CartService service, CurrentUser users) {
    this.service = service;
    this.users = users;
  }

  public record Add(@NotNull @Positive Long productId, @Min(1) @Max(99) int quantity) {}

  public record Quantity(@Min(1) @Max(99) int quantity) {}

  @GetMapping
  public CartService.Cart view(@AuthenticationPrincipal Jwt jwt) {
    return service.view(users.get(jwt));
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public CartItem add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Add request) {
    return service.add(users.get(jwt), request.productId(), request.quantity());
  }

  @PutMapping("/items/{id}")
  public CartItem update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody Quantity request) {
    return service.update(users.get(jwt), id, request.quantity());
  }

  @DeleteMapping("/items/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    service.delete(users.get(jwt), id);
  }
}
