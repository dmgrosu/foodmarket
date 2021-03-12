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

    public boolean needsUpdate(Good other) {
        if (other == null) {
            return false;
        }
        return (name != null && !name.equals(other.getName())) ||
                (price != null && !price.equals(other.getPrice())) ||
                (unit != null && !unit.equals(other.getUnit())) ||
                (inPackage != null && !inPackage.equals(other.inPackage)) ||
                (barCode != null && !barCode.equals(other.getBarCode())) ||
                (weight != null && !weight.equals(other.getWeight())) ||
                (brandId != null && brandId.equals(other.getBrandId())) ||
                (groupId != null && groupId.equals(other.getGroupId())) ||
                isDeleted();
    }
}
