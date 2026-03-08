package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
public class ProductGroupEntity {

    @Id
    private final Integer id;
    @NonNull
    private final String name;
    @Nullable
    @Column("parent_group_id")
    private final Integer parentGroupId;
    @Transient
    @Setter
    private List<ProductGroupEntity> childGroups = new ArrayList<>();
    @Nullable
    private final String erpCode;
    @NonNull
    private final Instant createdAt;
    @Nullable
    private final Instant deletedAt;
    @Nullable
    private final Instant updatedAt;

    @PersistenceCreator
    public ProductGroupEntity(Integer id, String name, Integer parentGroupId, String erpCode,
                              Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.parentGroupId = parentGroupId;
        this.erpCode = erpCode;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProductGroupEntity(String name, Integer parentGroupId, String erpCode) {
        this(null, name, parentGroupId, erpCode, Instant.now(), null, null);
    }

    public ProductGroupEntity(String name, String erpCode) {
        this(null, name, null, erpCode, Instant.now(), null, null);
    }

    public ProductGroupEntity withName(String name) {
        return new ProductGroupEntity(this.id,
                name,
                this.parentGroupId,
                this.erpCode,
                this.createdAt,
                Instant.now(),
                this.deletedAt
        );
    }

    public ProductGroupEntity withParentGroupId(Integer parentGroupId) {
        return new ProductGroupEntity(this.id,
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

    public void addChildIfAbsent(ProductGroupEntity child) {
        if (child == null) {
            return;
        }
        if (!this.childGroups.contains(child)) {
            this.childGroups.add(child);
        }
    }

    public boolean hasChildren() {
        return !childGroups.isEmpty();
    }

    public boolean hasParent() {
        return parentGroupId != null;
    }

}
