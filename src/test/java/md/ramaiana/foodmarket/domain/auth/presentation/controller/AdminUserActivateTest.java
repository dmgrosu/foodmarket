package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PUT /user/activate/{userId}.
 */
class AdminUserActivateTest extends MockedAuthenticationController {

  private static final String CONFIRMATION_TOKEN_PARAM = "confirmationToken=";

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private RegistrationTokenRepository registrationTokenRepository;

  @MockitoSpyBean
  private EmailSendUseCase emailSendUseCase;

  private String pendingEmail;

  @AfterEach
  void cleanUpPendingUser() {
    if (pendingEmail != null) {
      appUserRepository.findByEmail(pendingEmail).ifPresent(user -> {
        registrationTokenRepository.findAllForUser(user.getId())
            .forEach(token -> registrationTokenRepository.deleteById(token.getId()));
        appUserRepository.delete(user);
      });
      pendingEmail = null;
    }
  }

  private String activateUrl(Integer userId) {
    return "/user/activate/" + userId;
  }

  /**
   * A real CONFIRMED user, produced through the actual registration + confirm endpoints rather than
   * inserted directly — there is no other legitimate way to reach that state.
   */
  private AppUserEntity confirmedUser() throws Exception {
    pendingEmail = "activate-" + UUID.randomUUID() + "@example.com";
    AtomicReference<String> captured = new AtomicReference<>();
    doAnswer(invocation -> {
      EmailSendRequest request = invocation.getArgument(0);
      RegistrationConfirmationVariables variables = (RegistrationConfirmationVariables) request.variables();
      String url = variables.confirmationUrl();
      captured.set(url.substring(url.indexOf(CONFIRMATION_TOKEN_PARAM) + CONFIRMATION_TOKEN_PARAM.length()));
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(argThat(req -> req.variables() instanceof RegistrationConfirmationVariables));

    post("/auth/register", Map.of("email", pendingEmail, "password", TEST_PASSWORD, "clientId", 0))
        .andExpect(status().isOk());
    post("/auth/confirmEmail", Map.of("confirmationToken", captured.get()))
        .andExpect(status().isOk());

    return appUserRepository.findByEmail(pendingEmail).orElseThrow();
  }

  @Test
  void should_activate_a_confirmed_user() throws Exception {
    AppUserEntity user = confirmedUser();
    authenticateAs(Role.ADMIN);

    put(activateUrl(user.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.state").value("ACTIVE"));

    assertThat(appUserRepository.findById(user.getId()).orElseThrow().getState()).isEqualTo(UserState.ACTIVE);
  }

  @Test
  void should_reject_a_user_that_is_not_confirmed() throws Exception {
    AppUserEntity admin = authenticateAs(Role.ADMIN);

    // ACTIVE, not CONFIRMED — admin's own account is a convenient non-CONFIRMED subject.
    put(activateUrl(admin.getId())).andExpect(status().isBadRequest());
  }

  @Test
  void should_forbid_a_non_admin() throws Exception {
    AppUserEntity user = confirmedUser();
    authenticateAs(Role.USER);

    put(activateUrl(user.getId())).andExpect(status().isForbidden());
  }

  @Test
  void should_report_an_unknown_user_as_not_found() throws Exception {
    authenticateAs(Role.ADMIN);

    put(activateUrl(Integer.MAX_VALUE)).andExpect(status().isNotFound());
  }

  @Test
  void should_commit_the_activation_before_sending_the_email() throws Exception {
    // Same regression test as registration's: no transaction may be open while Mailjet is in
    // flight, and the ACTIVE state must already be committed by then.
    AppUserEntity user = confirmedUser();
    authenticateAs(Role.ADMIN);

    AtomicBoolean transactionActiveDuringSend = new AtomicBoolean(true);
    AtomicBoolean activeDuringSend = new AtomicBoolean(false);
    doAnswer(invocation -> {
      transactionActiveDuringSend.set(TransactionSynchronizationManager.isActualTransactionActive());
      activeDuringSend.set(appUserRepository.findById(user.getId())
          .map(u -> u.getState() == UserState.ACTIVE).orElse(false));
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(any());

    put(activateUrl(user.getId())).andExpect(status().isOk());

    assertThat(transactionActiveDuringSend).isFalse();
    assertThat(activeDuringSend).isTrue();
  }
}
