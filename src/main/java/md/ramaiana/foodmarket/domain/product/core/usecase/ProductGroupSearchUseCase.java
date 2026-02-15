package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.util.SpecificationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for searching product groups.
 */
@UseCase
@RequiredArgsConstructor
public class ProductGroupSearchUseCase {

  private final ProductGroupRepository productGroupRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ProductListResponse execute(@Nullable Integer parentGroupId) {
    List<ProductGroupEntity> groups = getGroupsHierarchy(parentGroupId);
    List<ProductGroupResponse> groupResponses = groups.stream()
        .map(ProductGroupResponse::new)
        .collect(Collectors.toList());

    return new ProductListResponse(Collections.emptyList(), groupResponses);
  }

  @NonNull
  private List<ProductGroupEntity> getGroupsHierarchy(@Nullable Integer parentGroupId) {
    SpecificationBuilder<ProductGroupEntity> specification = new SpecificationBuilder<>();

    // Always filter out deleted product groups
    specification.and(ProductGroupRepository.notDeleted());

    // Add parent group filter
    if (parentGroupId == null) {
      specification.and(ProductGroupRepository.parentGroupIsNull());
    } else {
      specification.and(ProductGroupRepository.parentGroupIdEquals(parentGroupId));
    }

    // Find groups with sorting by name
    List<ProductGroupEntity> foundGroups = productGroupRepository.findAll(
        specification.buildOrDefault(),
        Sort.by(Sort.Direction.ASC, "name")
    );

    // Recursively load child groups
    for (ProductGroupEntity foundGroup : foundGroups) {
      if (productGroupRepository.existsByParentGroupId(foundGroup.getId())) {
        List<ProductGroupEntity> children = getGroupsHierarchy(foundGroup.getId());
        foundGroup.setChildGroups(children);
      }
    }

    return foundGroups;
  }
}