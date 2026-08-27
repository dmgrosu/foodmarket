package md.ramaiana.foodmarket.domain.product.core.usecase;

import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
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
   * One level of the catalogue tree: the groups directly under {@code parentGroupId}, or the roots
   * when it is {@code null}.
   * <p>
   * A level at a time, not the whole tree. Walking it eagerly meant one query per group, and the ERP
   * export puts nearly ten thousand of them in there — roughly ten seconds of round trips to render
   * a sidebar whose top level is a few hundred entries.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ProductListResponse execute(@Nullable Integer storageId, @Nullable Integer parentGroupId) {
    Set<ProductGroupEntity> nonEmptyGroups = productGroupRepository.findAllNonEmpty(storageId);
    List<ProductGroupEntity> groups = getGroupsHierarchy(parentGroupId, nonEmptyGroups);
    List<ProductGroupResponse> groupResponses = groups.stream()
        .map(ProductGroupResponse::new)
        .collect(Collectors.toList());

    return new ProductListResponse(Collections.emptyList(), groupResponses);
  }

  /**
   * {@code nonEmptyGroups} is passed down rather than re-read: it does not vary with the level being
   * walked, and the query behind it scans the whole catalogue once per call.
   */
  @NonNull
  private List<ProductGroupEntity> getGroupsHierarchy(@Nullable Integer parentGroupId,
                                                      @NonNull Set<ProductGroupEntity> nonEmptyGroups) {
    Sort sort = Sort.by(Sort.Direction.ASC, "name");
    List<ProductGroupEntity> foundGroups = parentGroupId == null
        ? productGroupRepository.findByParentGroupIdIsNullAndDeletedAtIsNull(sort)
        : productGroupRepository.findByParentGroupIdAndDeletedAtIsNull(parentGroupId, sort);

    // Recursively load child groups and filter empty ones
    List<ProductGroupEntity> result = new ArrayList<>();
    for (ProductGroupEntity foundGroup : foundGroups) {
      List<ProductGroupEntity> children = getGroupsHierarchy(foundGroup.getId(), nonEmptyGroups);
      foundGroup.setChildGroups(children);
      if (!children.isEmpty() || nonEmptyGroups.contains(foundGroup)) {
        result.add(foundGroup);
      }
    }

    return result;
  }
}
