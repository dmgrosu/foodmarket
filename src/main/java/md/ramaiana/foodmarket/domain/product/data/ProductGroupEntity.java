package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.Objects;

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
    @Nullable
    private final String erpCode;
    @NonNull
    private final Instant createdAt;
    @Nullable
    private final Instant deletedAt;
    @Nullable
    private final Instant updatedAt;

    @PersistenceCreator
    public ProductGroupEntity(Integer id, @NonNull String name, @Nullable Integer parentGroupId, @Nullable String erpCode,
                              @NonNull Instant createdAt, @Nullable Instant updatedAt, @Nullable Instant deletedAt) {
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

    /**
     * Identity is the persisted row, not the object: the search use case matches groups loaded by one
     * query against groups loaded by another, so two instances of the same row have to compare equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductGroupEntity group)) return false;
        return Objects.equals(id, group.id) && Objects.equals(erpCode, group.erpCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, erpCode);
    }

}
