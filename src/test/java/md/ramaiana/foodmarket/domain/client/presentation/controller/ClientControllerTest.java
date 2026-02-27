package md.ramaiana.foodmarket.domain.client.presentation.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import md.ramaiana.foodmarket.BaseControllerTest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import org.junit.jupiter.api.Test;

class ClientControllerTest extends BaseControllerTest {

  @Test
  void whenClientExists_thenReturnsClient() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create("ACME", "1234567890123");

    doGet("/client/findByIdno?idno=1234567890123", user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("ACME"))
        .andExpect(jsonPath("$.idno").value("1234567890123"))
        .andExpect(jsonPath("$.id").value(client.getId()));
  }

  @Test
  void whenClientNotFound_thenReturns404() throws Exception {
    AppUserEntity user = testAppUserService.create();

    doGet("/client/findByIdno?idno=0000000000000", user)
        .andExpect(status().isNotFound());
  }
}
