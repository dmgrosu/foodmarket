package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * Request for listing orders.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListRequest {

  @NotNull
  private Long dateFrom;

  @NotNull
  private Long dateTo;

  @Nullable
  private Integer clientId;

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
