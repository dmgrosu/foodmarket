package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
import md.ramaiana.foodmarket.shared.util.SpecificationBuilder;
import org.springframework.transaction.annotation.Transactional;

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
    List<ProductEntity> products = findProductsFiltered(groupId, brandId, nameLike);
    List<ProductGroupEntity> groups = findProductsForProductsList(products);

    List<ProductResponse> productResponses = products.stream()
        .map(ProductResponse::new)
        .collect(Collectors.toList());

    List<ProductGroupResponse> groupResponses = groups.stream()
        .map(ProductGroupResponse::new)
        .collect(Collectors.toList());

    return new ProductListResponse(productResponses, groupResponses);
  }

  @NonNull
  private List<ProductEntity> findProductsFiltered(@Nullable Integer groupId, @Nullable Integer brandId, @Nullable String name) {
    SpecificationBuilder<@NonNull ProductEntity> specification = new SpecificationBuilder<>();

    // Always filter out deleted products
    specification.and(ProductRepository.notDeleted());

    // Add groupId filter if present
    if (groupId != null) {
      specification.and(ProductRepository.groupIdEquals(groupId));
    }

    // Add brandId filter if present
    if (brandId != null) {
      specification.and(ProductRepository.brandIdEquals(brandId));
    }

    // Add name filter if present
    if (name != null) {
      specification.and(ProductRepository.nameContainsIgnoreCase(name));
    }

    return productRepository.findAll(specification.buildOrDefault());
  }

  @NonNull
  private List<ProductGroupEntity> findProductsForProductsList(@NonNull List<ProductEntity> products) {
    Map<Integer, ProductGroupEntity> groupsMap = new HashMap<>();
    List<ProductGroupEntity> topGroups = new ArrayList<>();

    for (ProductEntity product : products) {
      Integer parentGroupId = product.getGroup() != null ? product.getGroup().getId() : null;
      if (parentGroupId != null) {
        addAllParentsToMap(parentGroupId, groupsMap);
      }
    }

    for (ProductGroupEntity group : groupsMap.values()) {
      if (!group.hasParent()) {
        topGroups.add(group);
      } else {
        ProductGroupEntity parent = groupsMap.get(group.getParentGroup().getId());
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
        addAllParentsToMap(group.getParentGroup().getId(), parents);
      }
    }
  }
}