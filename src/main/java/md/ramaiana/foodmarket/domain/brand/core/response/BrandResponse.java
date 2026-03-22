package md.ramaiana.foodmarket.domain.brand.core.response;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;

public record BrandResponse(
        Integer id,
        @NonNull
        String name
) {
    public BrandResponse(BrandEntity entity) {
        this(entity.getId(), entity.getName());
    }
}
