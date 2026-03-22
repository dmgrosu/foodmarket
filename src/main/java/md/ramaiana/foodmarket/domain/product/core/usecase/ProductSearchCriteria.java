package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;

public record ProductSearchCriteria(
        @Nullable Integer storageId,
        @Nullable Integer groupId,
        @Nullable Integer brandId,
        @Nullable String nameLike
) {
}
