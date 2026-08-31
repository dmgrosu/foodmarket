package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenRepository;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.request.EmailTemplateVariables;
import md.ramaiana.foodmarket.domain.email.core.request.PasswordResetVariables;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Fixture shared by the two password-reset endpoint tests. The raw reset token is captured off the
 * real {@link EmailSendUseCase} call rather than re-derived, so the tests exercise the link a user
 * would actually receive — the database only ever holds its hash.
 */
abstract class AbstractPasswordResetEndpointTest extends MockedAuthenticationController {

  protected static final String FORGOT_PASSWORD_URL = "/auth/forgotPassword";
  protected static final String RESET_PASSWORD_URL = "/auth/resetPassword";
  protected static final String LOGIN_URL = "/auth/login";
  protected static final String NEW_PASSWORD = "NewPassword456";

  private static final String RESET_TOKEN_PARAM = "resetToken=";

  @Autowired
  protected AppUserRepository appUserRepository;

  @Autowired
  protected PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoSpyBean
  protected EmailSendUseCase emailSendUseCase;

  private final List<Integer> resetUserIds = new ArrayList<>();

  /** Captures every EmailSendRequest the endpoints trigger, so "no email sent" is assertable. */
  protected final List<EmailTemplateVariables> sentEmails = new ArrayList<>();

  @BeforeEach
  void recordSentEmails() {
    sentEmails.clear();
    doAnswer(invocation -> {
      EmailSendRequest request = invocation.getArgument(0);
      sentEmails.add(request.variables());
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(any());
  }

  @AfterEach
  void cleanUpResetUsers() {
    // Tokens first: password_reset_token has a FK onto app_user.
    resetUserIds.forEach(id -> {
      passwordResetTokenRepository.findAllForUser(id)
          .forEach(token -> passwordResetTokenRepository.deleteById(token.getId()));
      appUserRepository.findById(id).ifPresent(appUserRepository::delete);
    });
    resetUserIds.clear();
  }

  /**
   * A user in the given state, registered for cleanup. Their password is {@link #TEST_PASSWORD}.
   */
  protected AppUserEntity userInState(UserState state) {
    AppUserEntity user = new AppUserEntity(
        "reset-" + UUID.randomUUID() + "@example.com",
        passwordEncoder.encode(TEST_PASSWORD),
        null,
        null,
        state,
        Language.RU
    );
    user.addRole(Role.USER);

    AppUserEntity saved = appUserRepository.save(user);
    resetUserIds.add(saved.getId());
    return saved;
  }

  protected AppUserEntity activeUser() {
    return userInState(UserState.ACTIVE);
  }

  protected Map<String, Object> forgotBody(String email) {
    return Map.of("email", email);
  }

  protected Map<String, Object> resetBody(String resetToken, String newPassword) {
    return Map.of("resetToken", resetToken, "newPassword", newPassword);
  }

  protected Map<String, Object> loginBody(String email, String password) {
    return Map.of("email", email, "password", password);
  }

  /**
   * Request a reset for the given address and return the raw token from the emailed link.
   */
  protected String requestResetAndCaptureToken(String email) throws Exception {
    AtomicReference<String> captured = new AtomicReference<>();
    doAnswer(invocation -> {
      EmailSendRequest request = invocation.getArgument(0);
      sentEmails.add(request.variables());
      PasswordResetVariables variables = (PasswordResetVariables) request.variables();
      captured.set(extractToken(variables.resetUrl()));
      return invocation.callRealMethod();
    }).when(emailSendUseCase).execute(any());

    post(FORGOT_PASSWORD_URL, forgotBody(email)).andReturn();

    assertThat(captured.get()).as("reset token in the emailed link").isNotNull();
    return captured.get();
  }

  protected PasswordResetTokenEntity latestResetTokenFor(AppUserEntity user) {
    return passwordResetTokenRepository.findAllForUser(user.getId()).getFirst();
  }

  /**
   * Rewrite a token's timestamps, so cooldown and expiry can be tested without sleeping.
   */
  protected void overwriteResetToken(PasswordResetTokenEntity token, java.time.Instant expiresAt,
                                     java.time.Instant createdAt) {
    passwordResetTokenRepository.save(new PasswordResetTokenEntity(
        token.getId(), token.getUser(), token.getTokenHash(), expiresAt, token.getUsedAt(), createdAt));
  }

  private static String extractToken(String resetUrl) {
    int idx = resetUrl.indexOf(RESET_TOKEN_PARAM);
    assertThat(idx).as("resetToken parameter in %s", resetUrl).isGreaterThanOrEqualTo(0);
    return resetUrl.substring(idx + RESET_TOKEN_PARAM.length());
  }
}
