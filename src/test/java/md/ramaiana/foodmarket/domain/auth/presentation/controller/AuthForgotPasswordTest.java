package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import java.time.Instant;
import java.util.Map;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/forgotPassword.
 * <p>
 * The endpoint is unauthenticated, so its central obligation is that the caller cannot tell a
 * registered address from an unregistered one. Several tests below assert on what was <i>not</i>
 * sent, because the HTTP response is required to be identical in both cases.
 */
class AuthForgotPasswordTest extends AbstractPasswordResetEndpointTest {

  @Test
  void should_email_a_reset_link_to_an_active_user() throws Exception {
    AppUserEntity user = activeUser();

    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.resetEmailSent").value(true));

    assertThat(sentEmails).hasSize(1);
  }

  @Test
  void should_persist_only_a_hash_of_the_emailed_token() throws Exception {
    AppUserEntity user = activeUser();

    String rawToken = requestResetAndCaptureToken(user.getEmail());

    PasswordResetTokenEntity stored = latestResetTokenFor(user);
    assertThat(stored.getTokenHash())
        .as("a database leak must not hand out working reset links")
        .isNotEqualTo(rawToken);
    assertThat(stored.isUsed()).isFalse();
  }

  @Test
  void should_answer_identically_for_an_unknown_address_and_send_nothing() throws Exception {
    // The whole point of the endpoint's shape: no account-existence oracle.
    post(FORGOT_PASSWORD_URL, forgotBody("nobody-" + System.nanoTime() + "@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resetEmailSent").value(true));

    assertThat(sentEmails).isEmpty();
  }

  @Test
  void should_send_nothing_to_a_user_who_has_not_confirmed_their_email() throws Exception {
    // They need /auth/resendConfirmation; a reset would not make the account usable.
    AppUserEntity user = userInState(UserState.PENDING_CONFIRMATION);

    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resetEmailSent").value(true));

    assertThat(sentEmails).isEmpty();
  }

  @Test
  void should_send_nothing_to_a_suspended_user() throws Exception {
    AppUserEntity user = userInState(UserState.SUSPENDED);

    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail())).andExpect(status().isOk());

    assertThat(sentEmails).isEmpty();
  }

  @Test
  void should_not_send_a_second_link_inside_the_cooldown() throws Exception {
    AppUserEntity user = activeUser();
    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail())).andExpect(status().isOk());

    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resetEmailSent").value(true));

    assertThat(sentEmails).as("cooldown must be silent, not an error").hasSize(1);
  }

  @Test
  void should_retire_the_previous_link_when_a_new_one_is_issued() throws Exception {
    AppUserEntity user = activeUser();
    String firstToken = requestResetAndCaptureToken(user.getEmail());

    // Age the first request out of the cooldown window without sleeping through it.
    PasswordResetTokenEntity issued = latestResetTokenFor(user);
    overwriteResetToken(issued, issued.getExpiresAt(), Instant.now().minusSeconds(3600));

    requestResetAndCaptureToken(user.getEmail());

    post(RESET_PASSWORD_URL, resetBody(firstToken, NEW_PASSWORD))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_an_invalid_email() throws Exception {
    post(FORGOT_PASSWORD_URL, Map.of("email", "not-an-email")).andExpect(status().isBadRequest());
  }

  @Test
  void should_not_require_authentication() throws Exception {
    authenticateAsAnonymous();
    AppUserEntity user = activeUser();

    post(FORGOT_PASSWORD_URL, forgotBody(user.getEmail())).andExpect(status().isOk());
  }
}
