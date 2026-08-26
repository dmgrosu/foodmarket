package md.ramaiana.foodmarket.domain.auth.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom (hand-written) AppUser Repository queries.
 */
public interface AppUserRepositoryCustom {

  /**
   * Search users, paginated.
   *
   * @param emailLike case-insensitive substring match on the email, or {@code null} for no filter.
   * @param state     exact match on the user's state, or {@code null} for no filter.
   * @param pageable  page, size and sort. Sort properties are validated against a column whitelist.
   */
  @NonNull
  Page<AppUserEntity> search(@Nullable String emailLike, @Nullable UserState state, @NonNull Pageable pageable);
}
