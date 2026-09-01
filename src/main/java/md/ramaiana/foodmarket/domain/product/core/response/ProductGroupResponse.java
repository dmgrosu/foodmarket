package md.ramaiana.foodmarket.domain.product.core.response;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;

/**
 * One node of the catalogue tree. A level is fetched at a time, so a node carries whether it is
 * worth expanding rather than the contents of everything below it.
 */
public record ProductGroupResponse(
        @NonNull
        Integer id,
        @NonNull
        String name,
        boolean hasChildren
) {
    public ProductGroupResponse(@NonNull ProductGroupEntity group, boolean hasChildren) {
        this(group.getId(), group.getName(), hasChildren);
    }
}
