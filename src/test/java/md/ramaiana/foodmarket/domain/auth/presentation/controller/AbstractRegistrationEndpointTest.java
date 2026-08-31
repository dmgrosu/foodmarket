package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Fixture shared by the three registration-confirmation endpoint tests: a real registration, and the
 * raw confirmation token captured off the real {@link EmailSendUseCase} call rather than re-derived —
 * so the tests exercise the link a user would actually receive.
 */
abstract class AbstractRegistrationEndpointTest extends MockedAuthenticationController {

  protected static final String REGISTER_URL = "/auth/register";
  protected static final String CONFIRM_EMAIL_URL = "/auth/confirmEmail";
  protected static final String RESEND_CONFIRMATION_URL = "/auth/resendConfirmation";

  private static final String CONFIRMATION_TOKEN_PARAM = "confirmationToken=";

  @Autowired
  protected AppUserRepository appUserRepository;

  @Autowired
  protected RegistrationTokenRepository registrationTokenRepository;

  @MockitoSpyBean
  protected EmailSendUseCase emailSendUseCase;

  private final List<String> registeredEmails = new ArrayList<>();

  @AfterEach
  void cleanUpRegisteredUsers() {
    registeredEmails.forEach(email -> appUserRepository.findByEmail(email).ifPresent(user -> {
      registrationTokenRepository.findAllForUser(user.getId())
          .forEach(token -> registrationTokenRepository.deleteById(token.getId()));
      appUserRepository.delete(user);
    }));
    registeredEmails.clear();
  }

  /**
   * An address no other test is using, registered for cleanup.
   */
  protected String uniqueEmail() {
    String email = "registration-" + UUID.randomUUID() + "@example.com";
    registeredEmails.add(email);
    return email;
  }

  protected Map<String, Object> registerBody(String email) {
    return Map.of("email", email, "password", TEST_PASSWORD, "clientId", 0);
  }

  protected Map<String, Object> confirmBody(String confirmationToken) {
    return Map.of("confirmationToken", confirmationToken);
  }

  /**
   * Register a user, returning the raw confirmation token from the emailed link.
   */
  protected String registerAndCaptureToken(String email) throws Exception {
    AtomicReference<String> captured = captureEmailedToken();

    post(REGISTER_URL, registerBody(email)).andReturn();

    assertThat(captured.get()).as("confirmation token in the emailed link").isNotNull();
    return captured.get();
  }

  /**
   * Start capturing the token from the next confirmation email sent.
   */
  protected AtomicReference<String> captureEmailedToken() {
    AtomicReference<String> captured = new AtomicReference<>();
    doAnswer(invocation -> {
      EmailSendRequest request = invocation.getArgument(0);
      RegistrationConfirmationVariables variables = (RegistrationConfirmationVariables) request.variables();
      captured.set(extractToken(variables.confirmationUrl()));
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(any());
    return captured;
  }

  /**
   * Move a registered user straight to ACTIVE and authenticate as them. Registration leaves a user
   * PENDING_CONFIRMATION, which every authenticated endpoint rejects; tests that only care about what
   * registration stored should not have to replay confirm-then-approve to get at it.
   */
  protected AppUserEntity activate(String email) {
    AppUserEntity user = appUserRepository.findByEmail(email).orElseThrow();
    AppUserEntity active = appUserRepository.save(user.withState(UserState.ACTIVE));
    authenticateAs(active);
    return active;
  }

  protected RegistrationTokenEntity latestTokenFor(String email) {
    AppUserEntity user = appUserRepository.findByEmail(email).orElseThrow();
    return registrationTokenRepository.findAllForUser(user.getId()).getFirst();
  }

  protected void overwriteToken(RegistrationTokenEntity token, Instant expiresAt, Instant createdAt) {
    registrationTokenRepository.save(new RegistrationTokenEntity(
        token.getId(), token.getUser(), token.getTokenHash(), expiresAt, token.getConfirmedAt(), createdAt));
  }

  private static String extractToken(String confirmationUrl) {
    int idx = confirmationUrl.indexOf(CONFIRMATION_TOKEN_PARAM);
    assertThat(idx).as("confirmationToken parameter in %s", confirmationUrl).isGreaterThanOrEqualTo(0);
    return confirmationUrl.substring(idx + CONFIRMATION_TOKEN_PARAM.length());
  }
}
