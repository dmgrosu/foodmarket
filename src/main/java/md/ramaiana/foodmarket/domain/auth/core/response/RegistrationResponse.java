package md.ramaiana.foodmarket.domain.auth.core.response;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.UserState;

/**
 * Response for registration and resend-confirmation. {@code confirmationEmailSent} tells the caller
 * whether the confirmation email actually went out, so the UI can offer a resend rather than claim
 * "check your inbox" when Mailjet failed.
 */
public record RegistrationResponse(
    @NonNull String email,
    @NonNull UserState state,
    boolean confirmationEmailSent
) {

  public RegistrationResponse(@NonNull AppUserEntity user, boolean confirmationEmailSent) {
    this(user.getEmail(), user.getState(), confirmationEmailSent);
  }
}
