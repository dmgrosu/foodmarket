package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table("product_group")
public class ProductGroup {
    @Id
    private final Integer id;
    private final String name;
    @Column("parent_group_id")
    private final Integer parentGroupId;
    @Column("erp_code")
    private final String erpCode;
    @Column("created_at")
    private final Instant createdAt;
    @Column("deleted_at")
    private final Instant deletedAt;
    @Column("updated_at")
    private final Instant updatedAt;
    @Transient
    @Setter
    private List<ProductGroup> childGroups = new ArrayList<>();

    @PersistenceCreator
    public ProductGroup(Integer id, String name, Integer parentGroupId, String erpCode,
                        Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.parentGroupId = parentGroupId;
        this.erpCode = erpCode;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProductGroup(String name, Integer parentGroupId, String erpCode) {
        this(null, name, parentGroupId, erpCode, Instant.now(), null, null);
    }

    public ProductGroup(String name, String erpCode) {
        this(null, name, null, erpCode, Instant.now(), null, null);
    }

    public ProductGroup withName(String name) {
        return new ProductGroup(this.id,
                name,
                this.parentGroupId,
                this.erpCode,
                this.createdAt,
                Instant.now(),
                this.deletedAt
        );
    }

    public ProductGroup withParentGroupId(Integer parentGroupId) {
        return new ProductGroup(this.id,
                this.name,
                parentGroupId,
                this.erpCode,
                this.createdAt,
                Instant.now(),
                this.deletedAt
        );
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
