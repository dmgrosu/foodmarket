package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@Table("product_group")
public class ProductGroup {
    @Id
    @With
    private final Integer id;
    private final String name;
    @Setter
    @Column("parent_group_id")
    private Integer parentGroupId;
    @Column("erp_code")
    private final String erpCode;
    @Column("created_at")
    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;
    @Column("updated_at")
    private final OffsetDateTime updatedAt;
    @Transient
    @Setter
    @Builder.Default
    private List<ProductGroup> childGroups = new ArrayList<>();

    public ProductGroup updateFrom(ProductGroup other) {
        return ProductGroup.builder()
                .name(other.getName())
                .parentGroupId(other.getParentGroupId())
                .erpCode(other.getErpCode())
                .createdAt(other.getCreatedAt())
                .deletedAt(other.getDeletedAt())
                .updatedAt(OffsetDateTime.now())
                .childGroups(other.getChildGroups())
                .build();
    }

    public boolean idDeleted() {
        return deletedAt != null;
    }

    public boolean hasChildren() {
        return !childGroups.isEmpty();
    }

    public boolean hasParent() {
        return parentGroupId != null && parentGroupId != 0;
    }

    public void addChildIfAbsent(ProductGroup child) {
        if (child == null) {
            return;
        }
        if (!this.childGroups.contains(child)) {
            this.childGroups.add(child);
        }
    }
}
