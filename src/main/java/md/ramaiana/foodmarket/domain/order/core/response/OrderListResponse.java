package md.ramaiana.foodmarket.domain.order.core.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Order list response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

  @NonNull
  private List<OrderResponse> orders;

  private int currentPage;

  private int pageSize;

  private int totalPages;
}
