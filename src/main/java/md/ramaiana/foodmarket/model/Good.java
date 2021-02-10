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
}
