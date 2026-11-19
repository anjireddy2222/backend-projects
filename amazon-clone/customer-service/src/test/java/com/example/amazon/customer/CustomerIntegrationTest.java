package com.example.amazon.customer;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.amazon.customer.cart.CatalogClient;
import com.example.amazon.customer.common.ApiException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CustomerIntegrationTest extends ApiTestSupport {
  @Test
  void cartTotalsQuantityAndOwnership() throws Exception {
    when(catalog.get(1L))
        .thenReturn(new CatalogClient.Product(1L, "Mouse", new BigDecimal("599.00"), 5));
    String owner = registerAndLogin(), other = registerAndLogin();
    long id =
        body(send(post("/api/cart/items"), owner, "{\"productId\":1,\"quantity\":2}")
                .andExpect(status().isCreated()))
            .get("id")
            .asLong();
    send(post("/api/cart/items"), owner, "{\"productId\":1,\"quantity\":1}")
        .andExpect(status().isCreated());
    send(get("/api/cart"), owner, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("items.length()").value(1))
        .andExpect(jsonPath("total").value(1797.0));
    send(put("/api/cart/items/" + id), owner, "{\"quantity\":6}")
        .andExpect(status().isBadRequest());
    send(put("/api/cart/items/" + id), other, "{\"quantity\":1}").andExpect(status().isNotFound());
    send(delete("/api/cart/items/" + id), other, null).andExpect(status().isNotFound());
    send(get("/api/cart"), other, null).andExpect(jsonPath("items.length()").value(0));
    send(put("/api/cart/items/" + id), owner, "{\"quantity\":1}").andExpect(status().isOk());
    send(get("/api/cart"), owner, null).andExpect(jsonPath("total").value(599.0));
    send(delete("/api/cart/items/" + id), owner, null).andExpect(status().isNoContent());
    send(get("/api/cart"), owner, null).andExpect(jsonPath("total").value(0));
  }

  @Test
  void catalogFailureDoesNotCreateCartItem() throws Exception {
    String token = registerAndLogin();
    when(catalog.get(999L)).thenThrow(ApiException.notFound("Product not found"));
    send(post("/api/cart/items"), token, "{\"productId\":999,\"quantity\":1}")
        .andExpect(status().isNotFound());
    when(catalog.get(1L))
        .thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog is unavailable"));
    send(post("/api/cart/items"), token, "{\"productId\":1,\"quantity\":1}")
        .andExpect(status().isServiceUnavailable());
    send(get("/api/cart"), token, null).andExpect(jsonPath("items.length()").value(0));
    send(post("/api/cart/items"), token, "{\"productId\":1,\"quantity\":0}")
        .andExpect(status().isBadRequest());
  }

  @Test
  void addressCrudAndOwnership() throws Exception {
    String owner = registerAndLogin(), other = registerAndLogin();
    String request =
        """
        {"recipient":"Student","phone":"9876543210","line1":"10 Main Road","city":"Chennai",
         "state":"Tamil Nadu","postalCode":"600001","country":"India"}
        """;
    long id =
        body(send(post("/api/addresses"), owner, request).andExpect(status().isCreated()))
            .get("id")
            .asLong();
    send(get("/api/addresses/" + id), owner, null).andExpect(status().isOk());
    send(get("/api/addresses/" + id), other, null).andExpect(status().isNotFound());
    send(put("/api/addresses/" + id), other, request).andExpect(status().isNotFound());
    send(delete("/api/addresses/" + id), other, null).andExpect(status().isNotFound());
    send(put("/api/addresses/" + id), owner, request.replace("Chennai", "Madurai"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("city").value("Madurai"));
    send(get("/api/addresses"), owner, null).andExpect(jsonPath("length()").value(1));
    send(get("/api/addresses"), other, null).andExpect(jsonPath("length()").value(0));
    send(post("/api/addresses"), owner, "{}").andExpect(status().isBadRequest());
    send(delete("/api/addresses/" + id), owner, null).andExpect(status().isNoContent());
    send(get("/api/addresses/" + id), owner, null).andExpect(status().isNotFound());
  }
}
