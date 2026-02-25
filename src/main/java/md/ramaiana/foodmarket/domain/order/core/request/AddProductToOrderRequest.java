package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to add product to order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToOrderRequest {

  private int orderId;

  @NotNull
  @Min(value = 1, message = "Product ID must be greater than 0")
  private Integer productId;

  @Min(value = 1, message = "Quantity must be greater than 0")
  private float quantity;

  @NotNull
  private Integer clientId;
}
