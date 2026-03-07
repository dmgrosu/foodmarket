package md.ramaiana.foodmarket.domain.product.core.response;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.price.core.response.PriceResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;

import java.util.List;


public record ProductResponse(
        @NonNull
        Integer id,
        @NonNull
        String name,
        @Nullable
        Integer groupId,
        @Nullable
        Integer brandId,
        @Nullable
        Float inPackage,
        @Nullable
        String barCode,
        @Nullable
        String unit,
        @Nullable
        Float weight,
        @NotNull
        List<PriceResponse> prices
) {
    public ProductResponse(ProductEntity entity) {
        this(entity.getId(),
                entity.getName(),
                entity.getGroupId(),
                entity.getBrandId(),
                entity.getInPackage(),
                entity.getBarCode(),
                entity.getUnit(),
                entity.getWeight(),
                entity.getPrices() != null ?
                        entity.getPrices().stream().map(PriceResponse::new).toList() :
                        List.of());
    }
}
