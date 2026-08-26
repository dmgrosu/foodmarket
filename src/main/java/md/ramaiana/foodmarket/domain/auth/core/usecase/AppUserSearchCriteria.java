package md.ramaiana.foodmarket.domain.auth.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the admin user search.
 */
public record AppUserSearchCriteria(
    @Nullable String emailLike,
    @Nullable UserState state,
    int pageNo,
    int pageSize,
    @NonNull String sortColumn,
    @NonNull Sort.Direction sortDirection
) {
}
