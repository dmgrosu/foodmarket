package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/login, focused on how the registration states gate signing in.
 */
class AuthLoginTest extends AbstractRegistrationEndpointTest {

  private static final String LOGIN_URL = "/auth/login";

  private Map<String, Object> loginBody(String email) {
    return Map.of("email", email, "password", TEST_PASSWORD);
  }

  @Test
  void should_issue_a_token_for_an_active_user() throws Exception {
    AppUserEntity user = authenticateAs(Role.USER);

    post(LOGIN_URL, loginBody(user.getEmail()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.user.email").value(user.getEmail()));
  }

  @Test
  void should_forbid_a_user_that_has_not_confirmed_their_email() throws Exception {
    // Would have been a 500 before AuthLoginUseCase stopped throwing raw SecurityException.
    String email = uniqueEmail();
    post(REGISTER_URL, registerBody(email)).andExpect(status().isOk());

    post(LOGIN_URL, loginBody(email))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_forbid_a_confirmed_user_awaiting_administrator_approval() throws Exception {
    String email = uniqueEmail();
    String rawToken = registerAndCaptureToken(email);
    post(CONFIRM_EMAIL_URL, confirmBody(rawToken)).andExpect(status().isOk());

    post(LOGIN_URL, loginBody(email))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_reject_a_wrong_password() throws Exception {
    AppUserEntity user = authenticateAs(Role.USER);

    post(LOGIN_URL, Map.of("email", user.getEmail(), "password", "WrongPassword123"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_an_invalid_email() throws Exception {
    post(LOGIN_URL, Map.of("email", "not-an-email", "password", TEST_PASSWORD))
        .andExpect(status().isBadRequest());
  }
}
