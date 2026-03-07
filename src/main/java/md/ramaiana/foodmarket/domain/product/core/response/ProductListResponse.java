package md.ramaiana.foodmarket.domain.product.core.response;

import lombok.NonNull;

import java.util.List;

public record ProductListResponse(
        @NonNull List<ProductResponse> products,
        @NonNull List<ProductGroupResponse> groups
) {
}
