package md.ramaiana.foodmarket.domain.auth.core.response;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;

import java.time.Instant;

/**
 * A row in the admin user listing.
 */
public record AppUserResponse(
    @NonNull Integer id,
    @NonNull String email,
    @NonNull UserState state,
    @NonNull Language language,
    @Nullable Integer clientId,
    @Nullable String clientName,
    @NonNull Instant createdAt
) {

  /**
   * @param clientName the linked client's name, or {@code null} if the user has none, or the client
   *                    could not be resolved. Callers resolve this in one batched query rather than
   *                    per row — see {@link md.ramaiana.foodmarket.domain.auth.core.usecase.AppUserSearchUseCase}.
   */
  public AppUserResponse(@NonNull AppUserEntity user, @Nullable String clientName) {
    this(user.getId(), user.getEmail(), user.getState(), user.getLanguage(),
        user.hasClient() ? user.getClient().getId() : null, clientName, user.getCreatedAt());
  }
}
