package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom (hand-written) Product Repository queries.
 * <p>
 * "In stock" throughout means the product has at least one {@code balances} row with a positive
 * quantity — for the given storage when one is named, in any storage otherwise. That condition is
 * a correlated subquery rather than a join, so a product stocked in several storages still counts
 * once and cannot inflate a page.
 */
public interface ProductRepositoryCustom {

  /**
   * Page over live, in-stock products matching the filters.
   *
   * @param storageId restrict the stock check to this storage, or {@code null} for any.
   * @param groupId   exact match on the product's group, or {@code null} for no filter.
   * @param brandId   exact match on the product's brand, or {@code null} for no filter.
   * @param nameLike  case-insensitive substring match on the product name, or {@code null} for
   *                  no filter.
   * @param pageable  page, size and sort. The sort property is validated against a column
   *                  whitelist and rejected otherwise.
   */
  @NonNull
  Page<ProductEntity> searchInStock(@Nullable Integer storageId, @Nullable Integer groupId,
                                    @Nullable Integer brandId, @Nullable String nameLike,
                                    @NonNull Pageable pageable);

  /**
   * The distinct groups holding at least one product that matches the same filters.
   * <p>
   * Only the group ids are read. Building the catalogue tree needs nothing else, and loading the
   * matching products as aggregates would pull every one of their prices along with them.
   */
  @NonNull
  List<Integer> findGroupIdsInStock(@Nullable Integer storageId, @Nullable Integer groupId,
                                    @Nullable Integer brandId, @Nullable String nameLike);

  /**
   * Product names by id, for the given ids.
   * <p>
   * Rendering an order means putting a name on every line. Doing that with one
   * {@code findNameById} per line costs a query per item per order across a whole page, so the whole
   * page's names are read once instead.
   *
   * @return only the ids that resolve to a live product; an empty map for an empty input.
   */
  @NonNull
  Map<Integer, String> findNamesByIds(@NonNull Collection<Integer> ids);
}
