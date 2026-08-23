package md.ramaiana.foodmarket.domain.brand.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the admin brand search.
 */
public record BrandSearchCriteria(
    @Nullable String nameLike,
    int pageNo,
    int pageSize,
    @NonNull String sortColumn,
    @NonNull Sort.Direction sortDirection
) {
}
