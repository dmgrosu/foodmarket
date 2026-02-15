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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;


/**
 * Product Group Entity.
 */
@Getter
@Setter
@Entity
@Table(name = "product_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductGroupEntity {

  @NonNull
  @Setter(AccessLevel.NONE)
  private final UUID uuid = UUID.randomUUID();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @NonNull
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_group_id")
  private ProductGroupEntity parentGroup;

  @OneToMany(mappedBy = "parentGroup", fetch = FetchType.LAZY)
  private List<ProductGroupEntity> childGroups = new ArrayList<>();

  @Nullable
  @Column(name = "erp_code", unique = true)
  private String erpCode;

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

  public boolean hasChildren() {
    return !childGroups.isEmpty();
  }

  public boolean hasParent() {
    return parentGroup != null;
  }

  public void addChildIfAbsent(ProductGroupEntity child) {
    if (child == null || this.childGroups.contains(child)) {
      return;
    }
    this.childGroups.add(child);
    child.setParentGroup(this);
  }
}
