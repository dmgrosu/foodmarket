package md.ramaiana.foodmarket.domain.product.core.response;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;

/**
 * Product response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  @NonNull
  private Integer id;

  @NonNull
  private String name;

  private float price;

  @Nullable
  private Integer groupId;

  @Nullable
  private Integer brandId;

  @Nullable
  private Float inPackage;

  @Nullable
  private String barCode;

  @Nullable
  private String unit;

  @Nullable
  private Float weight;

  public ProductResponse(@NonNull ProductEntity product) {
    this.id = product.getId();
    this.name = product.getName();
    this.price = product.getPrice();
    this.groupId = product.getGroupId();
    this.brandId = product.getBrandId();
    this.inPackage = product.getInPackage();
    this.barCode = product.getBarCode();
    this.unit = product.getUnit();
    this.weight = product.getWeight();
  }
}
