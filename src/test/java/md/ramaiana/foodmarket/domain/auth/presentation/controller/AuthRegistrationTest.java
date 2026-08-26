package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/register.
 */
class AuthRegistrationTest extends AbstractRegistrationEndpointTest {

  @Test
  void should_register_a_user_awaiting_confirmation() throws Exception {
    String email = uniqueEmail();

    post(REGISTER_URL, registerBody(email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.state").value("PENDING_CONFIRMATION"))
        .andExpect(jsonPath("$.confirmationEmailSent").value(true));

    AppUserEntity user = appUserRepository.findByEmail(email).orElseThrow();
    assertThat(user.getState()).isEqualTo(UserState.PENDING_CONFIRMATION);
    assertThat(user.getRoles()).containsExactly(Role.USER);
  }

  @Test
  void should_store_only_a_hash_of_the_emailed_token() throws Exception {
    String email = uniqueEmail();

    String rawToken = registerAndCaptureToken(email);

    RegistrationTokenEntity token = latestTokenFor(email);
    assertThat(token.getTokenHash()).isNotEqualTo(rawToken);
    assertThat(token.isConfirmed()).isFalse();
  }

  @Test
  void should_commit_the_user_before_sending_the_confirmation_email() throws Exception {
    // The regression test for the whole RequestHandler split: no transaction may be open while the
    // Mailjet call is in flight, and the user must already be committed by then.
    String email = uniqueEmail();
    AtomicBoolean transactionActiveDuringSend = new AtomicBoolean(true);
    AtomicBoolean userVisibleDuringSend = new AtomicBoolean(false);

    doAnswer(invocation -> {
      transactionActiveDuringSend.set(TransactionSynchronizationManager.isActualTransactionActive());
      userVisibleDuringSend.set(appUserRepository.findByEmail(email).isPresent());
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(any());

    post(REGISTER_URL, registerBody(email)).andExpect(status().isOk());

    assertThat(transactionActiveDuringSend).isFalse();
    assertThat(userVisibleDuringSend).isTrue();
  }

  @Test
  void should_keep_the_user_when_sending_the_confirmation_email_fails() throws Exception {
    String email = uniqueEmail();
    doThrow(new MailException("Mailjet is down")).when(emailSendUseCase).execute(any());

    post(REGISTER_URL, registerBody(email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("PENDING_CONFIRMATION"))
        .andExpect(jsonPath("$.confirmationEmailSent").value(false));

    assertThat(appUserRepository.findByEmail(email)).isPresent();
  }

  @Test
  void should_reject_an_already_registered_email() throws Exception {
    String email = uniqueEmail();
    post(REGISTER_URL, registerBody(email)).andExpect(status().isOk());

    post(REGISTER_URL, registerBody(email))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_an_invalid_email() throws Exception {
    post(REGISTER_URL, Map.of("email", "not-an-email", "password", TEST_PASSWORD, "clientId", 0))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_blank_password() throws Exception {
    post(REGISTER_URL, Map.of("email", uniqueEmail(), "password", "", "clientId", 0))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_be_reachable_without_authentication() throws Exception {
    // No Authorization header — proves the SecurityConfig permitAll + JwtFilter wiring.
    authenticateAsAnonymous();

    post(REGISTER_URL, registerBody(uniqueEmail()))
        .andExpect(status().isOk());
  }
}
