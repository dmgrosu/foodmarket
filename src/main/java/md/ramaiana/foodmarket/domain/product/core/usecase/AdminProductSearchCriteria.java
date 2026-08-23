package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the admin product search. Distinct from {@link ProductSearchCriteria}: this one
 * has no storage filter and is not restricted to products with positive stock.
 */
public record AdminProductSearchCriteria(
    @Nullable String nameLike,
    @Nullable Integer brandId,
    @Nullable Integer groupId,
    int pageNo,
    int pageSize,
    @NonNull String sortColumn,
    @NonNull Sort.Direction sortDirection
) {
}
