package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@AllArgsConstructor
@Data
@Builder
@Table("good_group")
public class GoodGroup {
    @Id
    Integer id;
    String name;
    @Column("parent_group_id")
    Integer parentGroupId;
    @Column("erp_code")
    String erpCode;
    @Column("created_at")
    OffsetDateTime createdAt;
    @Column("deleted_at")
    OffsetDateTime deletedAt;
    @Column("updated_at")
    OffsetDateTime updatedAt;
    @Transient
    List<GoodGroup> childGroups;

    @PersistenceConstructor
    public GoodGroup(Integer id, String name, Integer parentGroupId, String erpCode, OffsetDateTime createdAt, OffsetDateTime deletedAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.parentGroupId = parentGroupId;
        this.erpCode = erpCode;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.updatedAt = updatedAt;
    }

    public boolean idDeleted() {
        return deletedAt != null;
    }

    public boolean hasChildren() {
        return childGroups != null && !childGroups.isEmpty();
    }

    public boolean hasParent() {
        return parentGroupId != null && parentGroupId != 0;
    }

    public boolean updateIfChanged(GoodGroup other) {
        if (other == null) {
            return false;
        }
        boolean changed = false;
        if (name == null || !name.equals(other.getName())) {
            setName(other.getName());
            changed = true;
        }
        if (parentGroupId == null || !parentGroupId.equals(other.getParentGroupId())) {
            if (other.getParentGroupId() != null) {
                setParentGroupId(other.getParentGroupId());
                changed = true;
            }
        }
        if (idDeleted()) {
            setDeletedAt(null);
            changed = true;
        }
        if (changed) {
            setUpdatedAt(OffsetDateTime.now());
        }
        return changed;
    }

    public void addChildIfAbsent(GoodGroup child) {
        if (child == null) {
            return;
        }
        if (this.childGroups == null) {
            this.childGroups = new ArrayList<>();
        }
        if (!this.childGroups.contains(child)) {
            this.childGroups.add(child);
        }
    }
}
