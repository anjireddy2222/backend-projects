package com.example.amazon.customer.cart;

import com.example.amazon.customer.auth.User;
import com.example.amazon.customer.common.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
  private final CartRepository carts;
  private final CatalogClient catalog;

  public CartService(CartRepository carts, CatalogClient catalog) {
    this.carts = carts;
    this.catalog = catalog;
  }

  public record Item(
      Long id,
      Long productId,
      String name,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  public record Cart(List<Item> items, BigDecimal total) {}

  public Cart view(User user) {
    List<Item> items = carts.findByUserIdOrderById(user.id).stream().map(this::viewItem).toList();
    return new Cart(
        items, items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  public CartItem add(User user, Long productId, int quantity) {
    var product = catalog.get(productId);
    CartItem item = carts.findByUserIdAndProductId(user.id, productId).orElseGet(CartItem::new);
    int newQuantity = item.quantity + quantity;
    checkQuantity(newQuantity, product.stock());
    item.user = user;
    item.productId = productId;
    item.quantity = newQuantity;
    return carts.save(item);
  }

  public CartItem update(User user, Long id, int quantity) {
    CartItem item = owned(user, id);
    checkQuantity(quantity, catalog.get(item.productId).stock());
    item.quantity = quantity;
    return carts.save(item);
  }

  public void delete(User user, Long id) {
    carts.delete(owned(user, id));
  }

  private CartItem owned(User user, Long id) {
    return carts
        .findByIdAndUserId(id, user.id)
        .orElseThrow(() -> ApiException.notFound("Cart item not found"));
  }

  private void checkQuantity(int quantity, int stock) {
    if (quantity < 1 || quantity > 99 || quantity > stock)
      throw ApiException.badRequest("Quantity must be 1-99 and within available stock");
  }

  private Item viewItem(CartItem item) {
    var p = catalog.get(item.productId);
    return new Item(
        item.id,
        p.id(),
        p.name(),
        item.quantity,
        p.price(),
        p.price().multiply(BigDecimal.valueOf(item.quantity)));
  }
}
