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
    Sort sort = Sort.by(Sort.Direction.ASC, "name");
    List<ProductGroupEntity> foundGroups = parentGroupId == null
        ? productGroupRepository.findByParentGroupIdIsNullAndDeletedAtIsNull(sort)
        : productGroupRepository.findByParentGroupIdAndDeletedAtIsNull(parentGroupId, sort);

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
