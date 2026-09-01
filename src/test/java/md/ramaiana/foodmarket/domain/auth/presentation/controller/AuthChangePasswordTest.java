package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import java.util.Map;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PUT /auth/changePassword — the signed-in user rotating their own password.
 * <p>
 * Every success case proves the change by logging in afterwards rather than by trusting the 200:
 * an endpoint that returned OK without rewriting the hash would pass any weaker assertion.
 */
class AuthChangePasswordTest extends MockedAuthenticationController {

  private static final String CHANGE_PASSWORD_URL = "/auth/changePassword";
  private static final String LOGIN_URL = "/auth/login";
  private static final String NEW_PASSWORD = "NewPassword456";

  private Map<String, Object> loginBody(String email, String password) {
    return Map.of("email", email, "password", password);
  }

  @Test
  void should_change_the_password_so_only_the_new_one_signs_in() throws Exception {
    AppUserEntity user = authenticateAs(Role.USER);

    put(CHANGE_PASSWORD_URL, Map.of("currentPassword", TEST_PASSWORD, "newPassword", NEW_PASSWORD))
        .andExpect(status().isOk());

    post(LOGIN_URL, loginBody(user.getEmail(), TEST_PASSWORD)).andExpect(status().isUnauthorized());
    post(LOGIN_URL, loginBody(user.getEmail(), NEW_PASSWORD)).andExpect(status().isOk());
  }

  @Test
  void should_reject_a_wrong_current_password_and_leave_the_password_alone() throws Exception {
    AppUserEntity user = authenticateAs(Role.USER);

    put(CHANGE_PASSWORD_URL, Map.of("currentPassword", "WrongPassword123", "newPassword", NEW_PASSWORD))
        .andExpect(status().isBadRequest());

    post(LOGIN_URL, loginBody(user.getEmail(), TEST_PASSWORD)).andExpect(status().isOk());
  }

  @Test
  void should_reject_reusing_the_current_password() throws Exception {
    authenticateAs(Role.USER);

    put(CHANGE_PASSWORD_URL, Map.of("currentPassword", TEST_PASSWORD, "newPassword", TEST_PASSWORD))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_blank_new_password() throws Exception {
    authenticateAs(Role.USER);

    put(CHANGE_PASSWORD_URL, Map.of("currentPassword", TEST_PASSWORD, "newPassword", "  "))
        .andExpect(status().isBadRequest());
  }

  /**
   * 403, not 401: no AuthenticationEntryPoint is configured, so Spring Security's
   * ExceptionTranslationFilter answers an anonymous rejection with Forbidden. What matters here is
   * that the request never reaches the controller — the status itself is app-wide behaviour, not
   * something this endpoint chooses.
   */
  @Test
  void should_reject_an_anonymous_change() throws Exception {
    authenticateAsAnonymous();

    put(CHANGE_PASSWORD_URL, Map.of("currentPassword", TEST_PASSWORD, "newPassword", NEW_PASSWORD))
        .andExpect(status().isForbidden());
  }
}
