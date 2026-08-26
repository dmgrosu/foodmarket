package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Use case for issuing a registration confirmation token — the database half of confirming a
 * registration. Retires any token the user still holds, persists a fresh one, and builds the
 * confirmation link. Sends nothing: {@link RegistrationConfirmationMailUseCase} does that, outside
 * the transaction this use case runs in.
 * <p>
 * Because every issue retires the previous tokens first, a user has at most one usable
 * confirmation link at any moment.
 */
@UseCase
@RequiredArgsConstructor
@EnableConfigurationProperties(RegistrationProperties.class)
public class RegistrationTokenIssueUseCase {

  private final RegistrationTokenRepository registrationTokenRepository;
  private final SecureTokenGenerator secureTokenGenerator;
  private final RegistrationProperties registrationProperties;

  /**
   * Everything the side-effect phase needs to email the confirmation link. Internal plumbing between
   * a use case's transactional phase and its side-effect phase — never bound from or to HTTP.
   * <p>
   * The raw confirmation token lives only inside {@code confirmationUrl}, in memory: the database
   * holds nothing but its hash.
   */
  public record TransactionalEffectResult(
      @NonNull AppUserEntity user,
      @NonNull String confirmationUrl,
      int expiresInHours
  ) {
  }

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public TransactionalEffectResult execute(@NonNull AppUserEntity user) {
    Instant now = Instant.now();

    registrationTokenRepository.expireLiveTokensForUser(user.getId(), now);

    String rawToken = secureTokenGenerator.generate();
    Instant expiresAt = now.plus(registrationProperties.confirmationLinkValidityHours(), ChronoUnit.HOURS);
    registrationTokenRepository.save(new RegistrationTokenEntity(
        AggregateReference.to(user.getId()),
        secureTokenGenerator.hash(rawToken),
        expiresAt
    ));

    String confirmationUrl = UriComponentsBuilder.fromUriString(registrationProperties.confirmationPageUrl())
        .queryParam("confirmationToken", rawToken)
        .toUriString();

    return new TransactionalEffectResult(user, confirmationUrl, registrationProperties.confirmationLinkValidityHours());
  }
}
