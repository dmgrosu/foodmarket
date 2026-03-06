package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Use case for searching products.
 */
@UseCase
@RequiredArgsConstructor
public class ProductSearchUseCase {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;

    /**
     * Execute the use case.
     */
    @NonNull
    @Transactional(readOnly = true)
    public ProductListResponse execute(@Nullable Integer groupId, @Nullable Integer brandId, @Nullable String nameLike) {
        List<ProductEntity> products = productRepository.findAllByFilters(groupId, brandId, nameLike);
        List<ProductGroupEntity> groups = findProductsForProductsList(products);

        List<ProductResponse> productResponses = products.stream()
                .map(ProductResponse::new)
                .toList();

        List<ProductGroupResponse> groupResponses = groups.stream()
                .map(ProductGroupResponse::new)
                .toList();

        return new ProductListResponse(productResponses, groupResponses);
    }

    @NonNull
    private List<ProductGroupEntity> findProductsForProductsList(@NonNull List<ProductEntity> products) {
        Map<Integer, ProductGroupEntity> groupsMap = new HashMap<>();
        List<ProductGroupEntity> topGroups = new ArrayList<>();

        for (ProductEntity product : products) {
            Integer productGroupId = product.getGroupId();
            if (productGroupId != null) {
                addAllParentsToMap(productGroupId, groupsMap);
            }
        }

        for (ProductGroupEntity group : groupsMap.values()) {
            if (!group.hasParent()) {
                topGroups.add(group);
            } else {
                ProductGroupEntity parent = groupsMap.get(group.getParentGroupId());
                if (parent != null) {
                    parent.addChildIfAbsent(group);
                }
            }
        }

        return topGroups;
    }

    private void addAllParentsToMap(@NonNull Integer childGroupId, @NonNull Map<Integer, ProductGroupEntity> parents) {
        Optional<ProductGroupEntity> optionalGroup = productGroupRepository.findById(childGroupId);
        if (optionalGroup.isPresent()) {
            ProductGroupEntity group = optionalGroup.get();
            parents.putIfAbsent(group.getId(), group);
            if (group.hasParent()) {
                addAllParentsToMap(group.getParentGroupId(), parents);
            }
        }
    }
}
