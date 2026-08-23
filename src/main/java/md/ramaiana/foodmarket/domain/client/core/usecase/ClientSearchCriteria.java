package md.ramaiana.foodmarket.domain.client.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the admin client search.
 */
public record ClientSearchCriteria(
    @Nullable String nameLike,
    @Nullable String idno,
    int pageNo,
    int pageSize,
    @NonNull String sortColumn,
    @NonNull Sort.Direction sortDirection
) {
}
