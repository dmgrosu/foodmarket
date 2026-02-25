package md.ramaiana.foodmarket.domain.product.core.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;

/**
 * Product group response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroupResponse {

  @NonNull
  private Integer id;

  @NonNull
  private String name;

  @NonNull
  private List<ProductGroupResponse> children = new ArrayList<>();

  @NonNull
  private List<ProductResponse> products = new ArrayList<>();

  public ProductGroupResponse(@NonNull ProductGroupEntity group) {
    this.id = group.getId();
    this.name = group.getName();
    this.children = group.hasChildren()
        ? group.getChildGroups().stream().map(ProductGroupResponse::new).collect(Collectors.toList())
        : new ArrayList<>();
    this.products = new ArrayList<>();
  }
}
