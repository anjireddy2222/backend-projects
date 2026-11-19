package com.example.amazon.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class CatalogIntegrationTest {
  @Autowired MockMvc mvc;

  @Test
  void searchFilterAndDetails() throws Exception {
    mvc.perform(get("/api/products").param("q", "WIRELESS").param("category", "Electronics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("totalElements").value(1))
        .andExpect(jsonPath("content[0].name").value("Wireless Mouse"));
    mvc.perform(get("/api/products/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("price").value(599.0));
    mvc.perform(get("/api/products").param("q", "missing"))
        .andExpect(jsonPath("totalElements").value(0));
    mvc.perform(get("/api/products").param("size", "1").param("page", "1"))
        .andExpect(jsonPath("content.length()").value(1));
  }

  @Test
  void invalidPagesAndMissingProducts() throws Exception {
    mvc.perform(get("/api/products/9999")).andExpect(status().isNotFound());
    mvc.perform(get("/api/products").param("page", "-1")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/products").param("size", "101")).andExpect(status().isBadRequest());
  }
}
