package com.example.amazon.customer;

import static org.assertj.core.api.Assertions.*;

import com.example.amazon.customer.cart.CatalogClient;
import com.example.amazon.customer.common.ApiException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

class CatalogClientTest {
  private HttpServer server;
  private CatalogClient client;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.start();
    client = new CatalogClient("http://127.0.0.1:" + server.getAddress().getPort());
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  private void response(String path, int status, String body) {
    server.createContext(
        path,
        exchange -> {
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
  }

  @Test
  void readsProductFromRealHttpResponse() {
    response(
        "/api/products/7",
        200,
        """
        {"id":7,"name":"Mouse","price":599.00,"stock":4,"description":"Extra fields are allowed"}
        """);
    var product = client.get(7L);
    assertThat(product.name()).isEqualTo("Mouse");
    assertThat(product.price()).isEqualByComparingTo("599.00");
    assertThat(product.stock()).isEqualTo(4);
  }

  @Test
  void translatesMissingProductsAndServerFailures() {
    response("/api/products/8", 404, "{}");
    response("/api/products/9", 500, "{}");
    assertThatThrownBy(() -> client.get(8L))
        .isInstanceOfSatisfying(
            ApiException.class, ex -> assertThat(ex.status).isEqualTo(HttpStatus.NOT_FOUND));
    assertThatThrownBy(() -> client.get(9L))
        .isInstanceOfSatisfying(
            ApiException.class,
            ex -> assertThat(ex.status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
  }
}
