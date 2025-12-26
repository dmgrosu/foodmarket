package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Objects;

@AllArgsConstructor
@Builder
@Getter
@Table("product")
public class Product {
    @Id
    @With
    private final Integer id;
    private final String name;
    private final Float price;
    private final String unit;
    @Column("package")
    private final Float inPackage;
    @Column("erp_code")
    private final String erpCode;
    @Column("bar_code")
    private final String barCode;
    private final Float weight;
    @Setter
    @Column("brand_id")
    private Integer brandId;
    @Setter
    @Column("group_id")
    private Integer groupId;
    @Column("created_at")
    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;
    @Column("updated_at")
    private final OffsetDateTime updatedAt;

    public Product updateFrom(Product product) {
        return Product.builder()
                .id(id)
                .name(product.getName())
                .price(product.getPrice())
                .unit(product.getUnit())
                .inPackage(product.getInPackage())
                .erpCode(product.getErpCode())
                .barCode(product.getBarCode())
                .weight(product.getWeight())
                .brandId(product.getBrandId())
                .groupId(product.getGroupId())
                .createdAt(product.getCreatedAt())
                .deletedAt(product.getDeletedAt())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id) && Objects.equals(erpCode, product.erpCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, erpCode);
    }
}
