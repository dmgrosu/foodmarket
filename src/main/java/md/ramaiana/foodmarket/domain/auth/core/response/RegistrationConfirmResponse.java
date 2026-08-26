package md.ramaiana.foodmarket.domain.auth.core.response;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.UserState;

/**
 * Response for confirming a registration email.
 * <p>
 * Carries a session so the browser need not ask for credentials again. While {@code state} is
 * {@code CONFIRMED} the token grants nothing — every request re-checks the user's state — so the
 * caller must not treat it as being signed in until {@code state} is {@code ACTIVE}.
 * Field names match {@code AuthResponse} so the frontend stores both the same way.
 */
public record RegistrationConfirmResponse(
    @NonNull String email,
    @NonNull UserState state,
    @NonNull String token,
    int tokenTtl
) {

  public RegistrationConfirmResponse(@NonNull AppUserEntity user, @NonNull String token, int tokenTtl) {
    this(user.getEmail(), user.getState(), token, tokenTtl);
  }
}
