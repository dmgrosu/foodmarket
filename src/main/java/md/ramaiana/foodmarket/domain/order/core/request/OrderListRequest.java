package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * Request for listing the caller's orders over a period.
 * <p>
 * Carries no client id: orders are read for the authenticated user's own client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListRequest {

  @NotNull
  private Long dateFrom;

  @NotNull
  private Long dateTo;

  @NotNull
  @Min(value = 0, message = "Page number must be >= 0")
  private Integer pageNo;

  @NotNull
  @Min(value = 1, message = "Page size must be >= 1")
  private Integer pageSize;

  @NotNull
  private Sort.Direction sortDirection;

  @NotNull
  private String sortColumn;
}
