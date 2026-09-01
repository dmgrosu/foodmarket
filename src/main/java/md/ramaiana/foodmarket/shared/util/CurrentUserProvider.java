package md.ramaiana.foodmarket.shared.util;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.stereotype.Component;

/**
 * Injectable view of the authenticated user, for use cases.
 * <p>
 * Controllers must stay two lines long, so a use case that needs to know who is calling cannot be
 * handed the identity as an argument — it resolves it here instead.
 */
@Component
public class CurrentUserProvider {

  @NonNull
  public AppUserEntity getCurrentUser() {
    return CurrentUser.require();
  }

  /**
   * The client the caller orders on behalf of.
   *
   * @throws BadRequestException if no client is attached to the account. Registration attaches one,
   *                             so in practice this is an administrator reaching a customer endpoint.
   */
  @NonNull
  public Integer getCurrentClientId() {
    AppUserEntity user = getCurrentUser();

    if (!user.hasClient()) {
      throw new BadRequestException("No client is attached to this account");
    }

    return user.getClient().getId();
  }
}
