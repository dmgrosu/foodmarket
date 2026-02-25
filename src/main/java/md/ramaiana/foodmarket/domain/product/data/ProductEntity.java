package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Product Entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("product")
public class ProductEntity {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  @NonNull
  private String name;
  @NonNull
  private Float price;
  @Nullable
  private String unit;
  @Nullable
  @Column("package")
  private Float inPackage;
  @Nullable
  private String erpCode;
  @Nullable
  private String barCode;
  @Nullable
  private Float weight;
  @Nullable
  private Integer brandId;
  @Nullable
  private Integer groupId;
  @NonNull
  private Instant createdAt = Instant.now();
  @Nullable
  private Instant deletedAt;
  @Nullable
  private Instant updatedAt;
}
