package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Objects;


@Getter
@Builder
@AllArgsConstructor
@Table("brand")
public class Brand {
    @Id
    @With
    private final Integer id;
    private final String name;
    @Column("erp_code")
    private final String erpCode;
    @Column("created_at")
    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;

    public Brand updateFrom(Brand brand) {
        return Brand.builder()
                .id(id)
                .name(brand.getName())
                .erpCode(brand.getErpCode())
                .createdAt(brand.getCreatedAt())
                .deletedAt(brand.getDeletedAt())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Brand brand)) return false;
        return Objects.equals(id, brand.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
