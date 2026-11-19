package com.example.amazon.customer.cart;

import com.example.amazon.customer.common.ApiException;
import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class CatalogClient {
  private final RestClient client;

  public record Product(Long id, String name, BigDecimal price, int stock) {}

  public CatalogClient(@Value("${app.catalog.url}") String url) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(3));
    factory.setReadTimeout(Duration.ofSeconds(3));
    client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }

  public Product get(Long id) {
    try {
      Product product = client.get().uri("/api/products/{id}", id).retrieve().body(Product.class);
      if (product == null)
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Empty catalog response");
      return product;
    } catch (HttpClientErrorException.NotFound ex) {
      throw ApiException.notFound("Product not found");
    } catch (RestClientException ex) {
      throw new ApiException(
          HttpStatus.SERVICE_UNAVAILABLE, "Catalog is unavailable; please try again");
    }
  }
}
