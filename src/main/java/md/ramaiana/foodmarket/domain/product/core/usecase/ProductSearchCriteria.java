package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the paged product listing.
 */
public record ProductSearchCriteria(
        @Nullable Integer storageId,
        @Nullable Integer groupId,
        @Nullable Integer brandId,
        @Nullable String nameLike,
        int pageNo,
        int pageSize,
        @NonNull String sortColumn,
        @NonNull Sort.Direction sortDirection
) {
}
