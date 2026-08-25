package md.ramaiana.foodmarket.domain.client.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom (hand-written) Client Repository queries.
 */
public interface ClientRepositoryCustom {

  /**
   * Search non-deleted clients, paginated.
   *
   * @param nameLike case-insensitive substring match on the client name, or {@code null} for no filter.
   * @param idno     exact match on the client idno, or {@code null} for no filter.
   * @param pageable page, size and sort. Sort properties are validated against a column whitelist.
   */
  @NonNull
  Page<ClientEntity> search(@Nullable String nameLike, @Nullable String idno, @NonNull Pageable pageable);
}
