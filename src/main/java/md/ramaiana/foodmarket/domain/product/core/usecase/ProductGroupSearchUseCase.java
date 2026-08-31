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
  public List<ProductGroupResponse> execute(@Nullable Integer storageId, @Nullable Integer parentGroupId) {
    Set<ProductGroupEntity> visibleGroups = productGroupRepository.findVisible(storageId);
    Set<Integer> visibleIds = new HashSet<>();
    Set<Integer> parentsOfVisible = new HashSet<>();
    for (ProductGroupEntity group : visibleGroups) {
      visibleIds.add(group.getId());
      if (group.hasParent()) {
        parentsOfVisible.add(group.getParentGroupId());
      }
    }

    Sort byName = Sort.by(Sort.Direction.ASC, "name");
    List<ProductGroupEntity> level = parentGroupId == null
        ? productGroupRepository.findByParentGroupIdIsNullAndDeletedAtIsNull(byName)
        : productGroupRepository.findByParentGroupIdAndDeletedAtIsNull(parentGroupId, byName);

    return level.stream()
        .filter(group -> visibleIds.contains(group.getId()))
        .map(group -> new ProductGroupResponse(group, parentsOfVisible.contains(group.getId())))
        .collect(Collectors.toList());
  }
}
