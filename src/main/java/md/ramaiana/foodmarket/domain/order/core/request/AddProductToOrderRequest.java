package md.ramaiana.foodmarket.domain.order.core.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ramaiana.foodmarket.shared.enums.PriceType;

/**
 * Request to add a product to the caller's cart.
 * <p>
 * Carries no order or client id: the cart is the caller's single NEW order, resolved from the
 * authenticated user, so neither can be chosen by the caller.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToOrderRequest {

  @NotNull
  @Min(value = 1, message = "Storage ID must be greater than 0")
  private Integer storageId;

  @NotNull
  @Min(value = 1, message = "Product ID must be greater than 0")
  private Integer productId;

  /**
   * Validated, not Lombok's {@code @NonNull}: that generates a null check inside the setter Jackson
   * calls, which surfaces as a 500 rather than a 400 when the field is missing.
   */
  @NotNull
  private PriceType priceType;

  @Min(value = 1, message = "Quantity must be greater than 0")
  private float quantity;
}
