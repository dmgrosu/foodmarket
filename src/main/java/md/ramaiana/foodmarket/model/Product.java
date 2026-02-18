package md.ramaiana.foodmarket.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Table("product")
public class Product {
    @Id
    private final Integer id;
    private final String name;
    private final String unit;
    @Column("package")
    private final Float inPackage;
    @Column("erp_code")
    private final String erpCode;
    @Column("bar_code")
    private final String barCode;
    private final Float weight;
    @Column("brand_id")
    private final Integer brandId;
    @Column("group_id")
    private final Integer groupId;
    @Column("created_at")
    private final Instant createdAt;
    @Column("deleted_at")
    private final Instant deletedAt;
    @Column("updated_at")
    private final Instant updatedAt;
    @MappedCollection(keyColumn = "id", idColumn = "product_id")
    private final List<Price> prices;

    @PersistenceCreator
    public Product(Integer id, String name, String unit,
                   Float inPackage, String erpCode, String barCode,
                   Float weight, Integer brandId, Integer groupId,
                   Instant createdAt, Instant updatedAt, Instant deletedAt,
                   List<Price> prices) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.inPackage = inPackage;
        this.erpCode = erpCode;
        this.barCode = barCode;
        this.weight = weight;
        this.brandId = brandId;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.prices = prices;
    }

    public Product(String name, String unit, Float inPackage, String erpCode,
                   String barCode, Float weight) {
        this(null, name, unit, inPackage, erpCode, barCode,
                weight, null, null, Instant.now(), null, null, new ArrayList<>());
    }

    public Product(String name, String unit, Float inPackage, String erpCode,
                   String barCode, Float weight, Integer brandId, Integer groupId) {
        this(null, name, unit, inPackage, erpCode, barCode,
                weight, brandId, groupId, Instant.now(), null, null, new ArrayList<>());
    }

    public Product updateFrom(Product other) {
        return new Product(this.id, other.getName(), other.getUnit(),
                other.getInPackage(), other.getErpCode(), other.getBarCode(),
                other.getWeight(), other.getBrandId(), other.getGroupId(),
                this.createdAt, Instant.now(), other.getDeletedAt(),
                this.prices);
    }

    public Product withGroupId(Integer groupId) {
        return new Product(id, name, unit, inPackage, erpCode,
                barCode, weight, brandId, groupId, createdAt, updatedAt, deletedAt,
                prices);
    }

    public Product withBrandId(Integer brandId) {
        return new Product(id, name, unit, inPackage, erpCode,
                barCode, weight, brandId, groupId, createdAt, updatedAt, deletedAt,
                prices);
    }

    public float getPrice(PriceType priceType) {
        return prices == null ?
                0f :
                prices.stream()
                        .filter(p -> p.getType() == priceType)
                        .findFirst()
                        .orElse(new Price(0f))
                        .getPrice();
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
