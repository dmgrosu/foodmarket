package md.ramaiana.foodmarket.domain.price.core.response;

import md.ramaiana.foodmarket.domain.price.data.PriceEntity;

public record PriceResponse(
        String type,
        float price
) {
    public PriceResponse(PriceEntity entity) {
        this(entity.getType().name(),
                entity.getPrice());
    }
}
