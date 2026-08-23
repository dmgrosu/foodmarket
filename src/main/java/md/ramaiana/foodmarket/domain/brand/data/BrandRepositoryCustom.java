package md.ramaiana.foodmarket.domain.brand.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom (hand-written) Brand Repository queries.
 */
public interface BrandRepositoryCustom {

  /**
   * Search non-deleted brands, paginated.
   *
   * @param nameLike case-insensitive substring match on the brand name, or {@code null} for no filter.
   * @param pageable page, size and sort. Sort properties are validated against a column whitelist.
   */
  @NonNull
  Page<BrandEntity> search(@Nullable String nameLike, @NonNull Pageable pageable);
}
