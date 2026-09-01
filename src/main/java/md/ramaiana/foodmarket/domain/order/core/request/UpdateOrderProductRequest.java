package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to set the quantity of a line in the caller's cart outright, in contrast to
 * {@link AddProductToOrderRequest} which adds to it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderProductRequest {

  @NotNull
  private Integer productId;

  @Min(value = 1, message = "Quantity must be greater than 0")
  private float quantity;
}
