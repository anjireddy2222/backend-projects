package com.example.amazon.customer.address;

import com.example.amazon.customer.auth.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
  private final AddressService service;
  private final CurrentUser users;

  public AddressController(AddressService service, CurrentUser users) {
    this.service = service;
    this.users = users;
  }

  @GetMapping
  public List<Address> list(@AuthenticationPrincipal Jwt jwt) {
    return service.list(users.get(jwt));
  }

  @GetMapping("/{id}")
  public Address get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.get(users.get(jwt), id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Address add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddressRequest request) {
    return service.add(users.get(jwt), request);
  }

  @PutMapping("/{id}")
  public Address update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody AddressRequest request) {
    return service.update(users.get(jwt), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    service.delete(users.get(jwt), id);
  }
}
