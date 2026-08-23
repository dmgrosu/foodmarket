package md.ramaiana.foodmarket.domain.product.core.usecase;

import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for the admin product search. Unlike {@link ProductSearchUseCase}, this returns a
 * flat, paginated list of every non-deleted product (including zero-stock ones), with no
 * positive-balance filter and no group-tree assembly.
 */
@UseCase
@RequiredArgsConstructor
public class AdminProductSearchUseCase {

  /**
   * Sort is a free-text request parameter, so it is constrained to an explicit set of
   * entity properties rather than passed straight to the persistence layer.
   */
  private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "name", "erpCode", "createdAt", "updatedAt");

  private final ProductRepository productRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public PagedResponse<ProductResponse> execute(@NonNull AdminProductSearchCriteria criteria) {
    if (!SORTABLE_PROPERTIES.contains(criteria.sortColumn())) {
      throw new BadRequestException(String.format("Unknown sort column '%s'", criteria.sortColumn()));
    }

    Pageable pageable = PageRequest.of(criteria.pageNo(), criteria.pageSize(),
        Sort.by(criteria.sortDirection(), criteria.sortColumn()));

    Page<ProductEntity> page = productRepository.search(
        criteria.nameLike(), criteria.brandId(), criteria.groupId(), pageable);

    return new PagedResponse<>(page.map(ProductResponse::new));
  }
}
