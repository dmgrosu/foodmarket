package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Product Group Entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("product_group")
public class ProductGroupEntity {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  @NonNull
  private String name;
  @Nullable
  @Column("parent_group_id")
  private Integer parentGroupId;
  @Transient
  private List<ProductGroupEntity> childGroups = new ArrayList<>();
  @Nullable
  private String erpCode;
  @NonNull
  private Instant createdAt = Instant.now();
  @Nullable
  private Instant deletedAt;
  @Nullable
  private Instant updatedAt;

  public boolean hasChildren() {
    return !childGroups.isEmpty();
  }

  public boolean hasParent() {
    return parentGroupId != null;
  }

  public void addChildIfAbsent(ProductGroupEntity child) {
    if (child == null || this.childGroups.contains(child)) {
      return;
    }
    this.childGroups.add(child);
    child.setParentGroupId(this.id);
  }
}
