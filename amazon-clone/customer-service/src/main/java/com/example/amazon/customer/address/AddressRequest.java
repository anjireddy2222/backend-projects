package com.example.amazon.customer.address;

import jakarta.validation.constraints.*;

public record AddressRequest(
    @NotBlank @Size(max = 100) String recipient,
    @NotBlank @Pattern(regexp = "[+0-9 ()-]{7,20}") String phone,
    @NotBlank @Size(max = 200) String line1,
    @NotBlank @Size(max = 80) String city,
    @NotBlank @Size(max = 80) String state,
    @NotBlank @Size(max = 12) String postalCode,
    @NotBlank @Size(max = 80) String country) {}
