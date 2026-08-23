package md.ramaiana.foodmarket.domain.client.core.usecase;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

/**
 * Criteria for the client search.
 */
public record ClientSearchCriteria(
    @Nullable String nameLike,
    @Nullable String idno,
    int pageNo,
    int pageSize,
    @NonNull String sortColumn,
    @NonNull Sort.Direction sortDirection
) {

  /**
   * Criteria matching at most the one client carrying this idno.
   */
  @NonNull
  public static ClientSearchCriteria byIdno(@NonNull String idno) {
    return new ClientSearchCriteria(null, idno, 0, 1, "name", Sort.Direction.ASC);
  }
}
