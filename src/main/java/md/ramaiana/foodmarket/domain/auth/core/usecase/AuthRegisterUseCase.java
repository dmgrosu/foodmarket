package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for user registration.
 * <p>
 * Three-phase: {@link #preExecute} validates before any transaction is held,
 * {@link #executeTransactionalEffect} does the database write, and {@link #executeSideEffects}
 * emails the confirmation link after that write has committed. The sequencing lives in
 * {@code domain/auth/core/handler/AuthRegisterRequestHandler}.
 */
@UseCase
@RequiredArgsConstructor
public class AuthRegisterUseCase {

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final ClientFindByIdUseCase clientFindByIdUseCase;
  private final RegistrationTokenIssueUseCase registrationTokenIssueUseCase;
  private final RegistrationConfirmationMailUseCase registrationConfirmationMailUseCase;

  /**
   * Validate the request before any transaction is opened.
   */
  public void preExecute(@NonNull RegisterRequest request) {
    if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new BadRequestException(String.format("User with email '%s' already exists", request.getEmail()));
    }
  }

  /**
   * Persist the new user and issue a confirmation token.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public TransactionalEffectResult executeTransactionalEffect(@NonNull RegisterRequest request) {
    AppUserEntity user = new AppUserEntity(
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        UserState.PENDING_CONFIRMATION,
        Language.fromTag(request.getLanguage())
    );
    user.addRole(Role.USER);

    Integer clientId = request.getClientId();
    if (clientId != null && clientId > 0) {
      AggregateReference<ClientEntity, Integer> clientRef = AggregateReference.to(clientId);
      // Resolve eagerly so an invalid clientId fails registration instead of silently attaching nothing.
      clientFindByIdUseCase.execute(clientRef);
      user = user.withClient(clientRef);
    }

    AppUserEntity savedUser = appUserRepository.save(user);
    return registrationTokenIssueUseCase.execute(savedUser);
  }

  /**
   * Email the confirmation link. Runs after the transactional effect has committed.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  public boolean executeSideEffects(@NonNull TransactionalEffectResult transactionalEffectResult) {
    return registrationConfirmationMailUseCase.execute(transactionalEffectResult);
  }
}
