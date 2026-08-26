package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationConfirmResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for confirming a registration email via its magic-link token.
 * <p>
 * Idempotent: replaying a token for a user who is no longer {@link UserState#PENDING_CONFIRMATION}
 * simply returns that user's current state instead of failing — clicking the link twice, or a mail
 * scanner prefetching it, is the common case, not an error. Every other failure (unknown token,
 * expired token, replay while still pending) collapses to a single undifferentiated
 * {@link BadRequestException} so it never reveals whether a token ever existed.
 */
@UseCase
@RequiredArgsConstructor
public class RegistrationConfirmUseCase {

  private static final String INVALID_TOKEN_MESSAGE = "Confirmation link is invalid or expired";

  private final RegistrationTokenRepository registrationTokenRepository;
  private final AppUserRepository appUserRepository;
  private final AppUserFindByIdUseCase appUserFindByIdUseCase;
  private final SecureTokenGenerator secureTokenGenerator;
  private final JwtCreateTokenUseCase jwtCreateTokenUseCase;


  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public RegistrationConfirmResponse execute(@NonNull RegistrationConfirmRequest request) {
    RegistrationTokenEntity token = registrationTokenRepository
        .findByTokenHash(secureTokenGenerator.hash(request.confirmationToken()))
        .orElseThrow(() -> new BadRequestException(INVALID_TOKEN_MESSAGE));

    AppUserEntity user = appUserFindByIdUseCase.execute(token.getUser().getId());
    if (user.getState() != UserState.PENDING_CONFIRMATION) {
      // Already confirmed (or further along, e.g. ACTIVE) — replaying the link is a success, not an error.
      return sessionFor(user);
    }

    Instant now = Instant.now();
    if (token.isConfirmed() || token.isExpired(now)) {
      throw new BadRequestException(INVALID_TOKEN_MESSAGE);
    }

    registrationTokenRepository.save(token.withConfirmedAt(now));
    return sessionFor(appUserRepository.save(user.withState(UserState.CONFIRMED)));
  }

  private RegistrationConfirmResponse sessionFor(@NonNull AppUserEntity user) {
    return new RegistrationConfirmResponse(
        user,
        jwtCreateTokenUseCase.execute(user),
        jwtCreateTokenUseCase.getTokenValidityInSeconds()
    );
  }
}
