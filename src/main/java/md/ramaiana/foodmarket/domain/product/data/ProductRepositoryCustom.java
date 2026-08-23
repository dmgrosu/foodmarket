package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom (hand-written) Product Repository queries.
 */
public interface ProductRepositoryCustom {

  /**
   * Search non-deleted products, paginated. Unlike {@code findAllByFiltersHavingPositiveBalance},
   * this is not restricted to products with positive stock — an admin listing needs to show
   * zero-stock products too.
   *
   * @param nameLike case-insensitive substring match on the product name, or {@code null} for no filter.
   * @param brandId  exact match on the brand id, or {@code null} for no filter.
   * @param groupId  exact match on the product group id, or {@code null} for no filter.
   * @param pageable page, size and sort. Sort properties are validated against a column whitelist.
   */
  @NonNull
  Page<ProductEntity> search(@Nullable String nameLike, @Nullable Integer brandId, @Nullable Integer groupId,
                              @NonNull Pageable pageable);
}
