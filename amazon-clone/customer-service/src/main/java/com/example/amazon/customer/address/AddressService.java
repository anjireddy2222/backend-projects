package com.example.amazon.customer.address;

import com.example.amazon.customer.auth.User;
import com.example.amazon.customer.common.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressService {
  private final AddressRepository addresses;

  public AddressService(AddressRepository addresses) {
    this.addresses = addresses;
  }

  public List<Address> list(User user) {
    return addresses.findByUserIdOrderById(user.id);
  }

  public Address get(User user, Long id) {
    return addresses
        .findByIdAndUserId(id, user.id)
        .orElseThrow(() -> ApiException.notFound("Address not found"));
  }

  public Address add(User user, AddressRequest request) {
    Address a = new Address();
    a.user = user;
    return save(a, request);
  }

  public Address update(User user, Long id, AddressRequest request) {
    return save(get(user, id), request);
  }

  public void delete(User user, Long id) {
    addresses.delete(get(user, id));
  }

  private Address save(Address a, AddressRequest r) {
    a.recipient = r.recipient();
    a.phone = r.phone();
    a.line1 = r.line1();
    a.city = r.city();
    a.state = r.state();
    a.postalCode = r.postalCode();
    a.country = r.country();
    return addresses.save(a);
  }
}
