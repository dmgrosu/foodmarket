package md.ramaiana.foodmarket.domain.brand.core.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;


/**
 * Brand response.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandResponse {

  private Integer id;

  @NonNull
  private String name;

  public BrandResponse(@NonNull BrandEntity entity) {
    this.id = entity.getId();
    this.name = entity.getName();
  }
}


