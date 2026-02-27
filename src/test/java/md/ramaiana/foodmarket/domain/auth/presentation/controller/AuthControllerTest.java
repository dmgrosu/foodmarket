package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import md.ramaiana.foodmarket.BaseControllerTest;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.testservices.TestAppUserService;
import org.junit.jupiter.api.Test;

class AuthControllerTest extends BaseControllerTest {

  @Test
  void whenLogin_thenReturnsToken() throws Exception {
    AppUserEntity user = testAppUserService.create();

    doPost("/auth/login", new LoginRequest(user.getEmail(), TestAppUserService.DEFAULT_PASSWORD))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.startsWith("Bearer ")))
        .andExpect(jsonPath("$.user.email").value(user.getEmail()))
        .andExpect(jsonPath("$.tokenTtl").isNumber());
  }

  @Test
  void whenLoginWrongPassword_thenReturnsError() throws Exception {
    AppUserEntity user = testAppUserService.create();

    doPost("/auth/login", new LoginRequest(user.getEmail(), "wrong-password"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenRegister_thenReturnsToken() throws Exception {
    doPost("/auth/register", new RegisterRequest("newuser@test.com", "pass123", null))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.user.email").value("newuser@test.com"));
  }

  @Test
  void whenRegisterDuplicateEmail_thenReturns400() throws Exception {
    testAppUserService.create("taken@test.com", md.ramaiana.foodmarket.shared.enums.Role.USER);

    doPost("/auth/register", new RegisterRequest("taken@test.com", "pass123", null))
        .andExpect(status().isBadRequest());
  }
}
