package md.ramaiana.foodmarket.domain.product.core.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Product list response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {

  @NonNull
  private List<ProductResponse> products;

  @NonNull
  private List<ProductGroupResponse> groups;
}
