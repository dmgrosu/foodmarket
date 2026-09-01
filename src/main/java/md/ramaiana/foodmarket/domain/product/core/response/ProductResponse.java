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
        String groupName,
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
    /**
     * @param groupName the name of the product's group, so a result found by searching across the
     *                  whole catalogue still says which part of it it came from.
     */
    public ProductResponse(ProductEntity entity, @Nullable String groupName) {
        this(entity.getId(),
                entity.getName(),
                entity.getGroupId(),
                groupName,
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
