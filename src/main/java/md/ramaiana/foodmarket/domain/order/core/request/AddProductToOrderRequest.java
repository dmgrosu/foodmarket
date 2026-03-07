package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.PriceType;

/**
 * Request to add product to order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToOrderRequest {

  private int orderId;

  @NotNull
  @Min(value = 1, message = "Storage ID must be greater than 0")
  private Integer storageId;

  @NotNull
  @Min(value = 1, message = "Product ID must be greater than 0")
  private Integer productId;

  @NonNull
  private PriceType priceType;

  @Min(value = 1, message = "Quantity must be greater than 0")
  private float quantity;

  @NotNull
  private Integer clientId;
}
