package md.ramaiana.foodmarket.domain.brand.data;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Brand Entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("brand")
public class BrandEntity {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  @NonNull
  private String name;
  @NonNull
  private String erpCode;
}
