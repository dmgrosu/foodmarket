package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to update order product quantity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

  @NotNull
  private Integer orderId;

  @NotNull
  private Integer productId;

  @Min(value = 1, message = "Quantity must be greater than 0")
  private float quantity;
}
