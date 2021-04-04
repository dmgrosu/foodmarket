package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@AllArgsConstructor
@Data
@Builder
@Table("good")
public class Good {
    @Id
    Integer id;
    String name;
    Float price;
    String unit;
    @Column("package")
    Float inPackage;
    @Column("erp_code")
    String erpCode;
    @Column("bar_code")
    String barCode;
    Float weight;
    @Column("brand_id")
    Integer brandId;
    @Column("group_id")
    Integer groupId;
    @Column("created_at")
    OffsetDateTime createdAt;
    @Column("deleted_at")
    OffsetDateTime deletedAt;
    @Column("updated_at")
    OffsetDateTime updatedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean updateIfChanged(Good other) {
        if (other == null) {
            return false;
        }
        boolean changed = false;
        if (name == null || !name.equals(other.getName())) {
            setName(other.getName());
            changed = true;
        }
        if (barCode == null || !barCode.equals(other.getBarCode())) {
            setBarCode(other.getBarCode());
            changed = true;
        }
        if (price == null || !price.equals(other.getPrice())) {
            setPrice(other.getPrice());
            changed = true;
        }
        if (weight == null || !weight.equals(other.getWeight())) {
            setWeight(other.getWeight());
            changed = true;
        }
        if (brandId == null || brandId.equals(other.getBrandId())) {
            setBrandId(other.getBrandId());
            changed = true;
        }
        if (groupId == null || !groupId.equals(other.getGroupId())) {
            setGroupId(other.getGroupId());
            changed = true;
        }
        if (inPackage == null || !inPackage.equals(other.getInPackage())) {
            setInPackage(other.getInPackage());
            changed = true;
        }
        if (unit == null || !unit.equals(other.getUnit())) {
            setUnit(other.getUnit());
            changed = true;
        }
        if (isDeleted()) {
            setDeletedAt(null);
            changed = true;
        }
        if (changed) {
            setUpdatedAt(OffsetDateTime.now());
        }
        return changed;
    }
}
