package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import java.time.Instant;
import java.util.Map;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/resetPassword.
 * <p>
 * Unknown, spent and expired tokens all answer 400 with the same message: the endpoint is
 * unauthenticated, so it must not reveal whether a token ever existed.
 */
class AuthResetPasswordTest extends AbstractPasswordResetEndpointTest {

  @Test
  void should_set_the_new_password_so_only_it_signs_in() throws Exception {
    AppUserEntity user = activeUser();
    String rawToken = requestResetAndCaptureToken(user.getEmail());

    post(RESET_PASSWORD_URL, resetBody(rawToken, NEW_PASSWORD)).andExpect(status().isOk());

    post(LOGIN_URL, loginBody(user.getEmail(), TEST_PASSWORD)).andExpect(status().isUnauthorized());
    post(LOGIN_URL, loginBody(user.getEmail(), NEW_PASSWORD)).andExpect(status().isOk());
  }

  @Test
  void should_spend_the_token_so_the_link_works_only_once() throws Exception {
    AppUserEntity user = activeUser();
    String rawToken = requestResetAndCaptureToken(user.getEmail());
    post(RESET_PASSWORD_URL, resetBody(rawToken, NEW_PASSWORD)).andExpect(status().isOk());

    post(RESET_PASSWORD_URL, resetBody(rawToken, "ThirdPassword789"))
        .andExpect(status().isBadRequest());

    // The replay must not have taken effect either.
    post(LOGIN_URL, loginBody(user.getEmail(), NEW_PASSWORD)).andExpect(status().isOk());
  }

  @Test
  void should_reject_an_expired_token() throws Exception {
    AppUserEntity user = activeUser();
    String rawToken = requestResetAndCaptureToken(user.getEmail());

    PasswordResetTokenEntity issued = latestResetTokenFor(user);
    overwriteResetToken(issued, Instant.now().minusSeconds(1), issued.getCreatedAt());

    post(RESET_PASSWORD_URL, resetBody(rawToken, NEW_PASSWORD)).andExpect(status().isBadRequest());
    post(LOGIN_URL, loginBody(user.getEmail(), TEST_PASSWORD)).andExpect(status().isOk());
  }

  @Test
  void should_reject_an_unknown_token() throws Exception {
    post(RESET_PASSWORD_URL, resetBody("not-a-real-token", NEW_PASSWORD))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_blank_new_password() throws Exception {
    AppUserEntity user = activeUser();
    String rawToken = requestResetAndCaptureToken(user.getEmail());

    post(RESET_PASSWORD_URL, Map.of("resetToken", rawToken, "newPassword", "  "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_blank_token() throws Exception {
    post(RESET_PASSWORD_URL, Map.of("resetToken", "  ", "newPassword", NEW_PASSWORD))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_not_require_authentication() throws Exception {
    authenticateAsAnonymous();
    AppUserEntity user = activeUser();
    String rawToken = requestResetAndCaptureToken(user.getEmail());

    post(RESET_PASSWORD_URL, resetBody(rawToken, NEW_PASSWORD)).andExpect(status().isOk());
  }
}
