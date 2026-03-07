package md.ramaiana.foodmarket.domain.price.core.response;

import md.ramaiana.foodmarket.domain.price.data.PriceEntity;

public record PriceResponse(
        int id,
        String type,
        float price
) {
    public PriceResponse(PriceEntity entity) {
        this(entity.getId(),
                entity.getType().name(),
                entity.getPrice());
    }
}
