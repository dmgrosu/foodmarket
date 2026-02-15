package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;

/**
 * Product Entity.
 */
@Getter
@Setter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {

  @NonNull
  @Setter(AccessLevel.NONE)
  private final UUID uuid = UUID.randomUUID();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @NonNull
  private String name;

  @NonNull
  private Float price;

  @Nullable
  private String unit;

  @Nullable
  @Column(name = "package")
  private Float inPackage;

  @Nullable
  @Column(name = "erp_code")
  private String erpCode;

  @Nullable
  @Column(name = "bar_code")
  private String barCode;

  @Nullable
  private Float weight;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private BrandEntity brand;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private ProductGroupEntity group;

  @NonNull
  @Setter(AccessLevel.NONE)
  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @Nullable
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Nullable
  @Column(name = "updated_at")
  private Instant updatedAt;
}
