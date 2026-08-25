package md.ramaiana.foodmarket.shared.response;

import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;

/**
 * Generic paginated response envelope.
 */
public record PagedResponse<T>(
    @NonNull List<T> items,
    int currentPage,
    int pageSize,
    int totalPages,
    long totalElements
) {
  public PagedResponse(@NonNull Page<T> page) {
    this(page.getContent(), page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements());
  }
}
