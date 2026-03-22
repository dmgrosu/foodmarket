package md.ramaiana.foodmarket.domain.product.core.response;

import lombok.NonNull;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ProductGroupResponse(
        @NonNull
        Integer id,
        @NonNull
        String name,
        @NonNull
        List<ProductGroupResponse> children,
        @NonNull
        List<ProductResponse> products
) {
    public ProductGroupResponse(@NonNull ProductGroupEntity group) {
        this(group.getId(), group.getName(),
                group.hasChildren()
                        ? group.getChildGroups().stream().map(ProductGroupResponse::new).collect(Collectors.toList())
                        : new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
