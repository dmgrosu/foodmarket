package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/resendConfirmation.
 */
class AuthResendConfirmationTest extends AbstractRegistrationEndpointTest {

  /**
   * Registers a user and backdates its token past the resend cooldown, which is what a real user
   * waiting for a late email would experience.
   */
  private String registerPastTheCooldown(String email) throws Exception {
    String firstToken = registerAndCaptureToken(email);
    RegistrationTokenEntity token = latestTokenFor(email);
    overwriteToken(token, token.getExpiresAt(), Instant.now().minusSeconds(3600));
    return firstToken;
  }

  @Test
  void should_issue_a_working_new_link_and_kill_the_previous_one() throws Exception {
    String email = uniqueEmail();
    String firstToken = registerPastTheCooldown(email);
    AtomicReference<String> secondToken = captureEmailedToken();

    post(RESEND_CONFIRMATION_URL, Map.of("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.state").value("PENDING_CONFIRMATION"))
        .andExpect(jsonPath("$.confirmationEmailSent").value(true));

    assertThat(secondToken.get()).isNotNull().isNotEqualTo(firstToken);

    post(CONFIRM_EMAIL_URL, confirmBody(firstToken))
        .andExpect(status().isBadRequest());
    post(CONFIRM_EMAIL_URL, confirmBody(secondToken.get()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("CONFIRMED"));
  }

  @Test
  void should_reject_a_resend_inside_the_cooldown() throws Exception {
    String email = uniqueEmail();
    registerAndCaptureToken(email);

    post(RESEND_CONFIRMATION_URL, Map.of("email", email))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_user_that_is_no_longer_awaiting_confirmation() throws Exception {
    String email = uniqueEmail();
    String rawToken = registerPastTheCooldown(email);
    post(CONFIRM_EMAIL_URL, confirmBody(rawToken)).andExpect(status().isOk());

    post(RESEND_CONFIRMATION_URL, Map.of("email", email))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_report_an_unknown_email_as_not_found() throws Exception {
    post(RESEND_CONFIRMATION_URL, Map.of("email", "nobody-" + System.nanoTime() + "@example.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_reject_an_invalid_email() throws Exception {
    post(RESEND_CONFIRMATION_URL, Map.of("email", "not-an-email"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_be_reachable_without_authentication() throws Exception {
    // No Authorization header — reaching the 404 rather than a 401/403 proves the request got through.
    authenticateAsAnonymous();

    post(RESEND_CONFIRMATION_URL, Map.of("email", "nobody-" + System.nanoTime() + "@example.com"))
        .andExpect(status().isNotFound());
  }
}
