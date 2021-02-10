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
@Table("brand")
public class Brand {
    @Id
    Integer id;
    String name;
    @Column("erp_code")
    String erpCode;
    @Column("created_at")
    OffsetDateTime createdAt;
    @Column("deleted_at")
    OffsetDateTime deletedAt;
}
