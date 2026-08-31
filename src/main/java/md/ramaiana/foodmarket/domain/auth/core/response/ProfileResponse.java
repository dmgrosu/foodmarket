package md.ramaiana.foodmarket.domain.auth.core.response;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Set;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;

/**
 * The signed-in user's own account, as rendered by the profile page.
 * <p>
 * {@code language} is the i18next tag ("ru", "ro", "en"), not the enum name — the frontend feeds it
 * straight back to i18next, the same shape it sends on the way in at registration.
 */
public record ProfileResponse(
    Integer id,
    @NonNull String email,
    @Nullable String firstName,
    @Nullable String lastName,
    @NonNull UserState state,
    @NonNull Set<Role> roles,
    @NonNull String language,
    @NonNull Instant createdAt,
    @Nullable ClientResponse client
) {

  public ProfileResponse(@NonNull AppUserEntity user, @Nullable ClientResponse client) {
    this(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getState(),
        user.getRoles(),
        user.getLanguage().getTag(),
        user.getCreatedAt(),
        client
    );
  }
}
