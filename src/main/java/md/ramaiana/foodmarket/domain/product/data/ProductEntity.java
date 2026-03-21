package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Product Entity.
 */
@Getter
@Table("product")
public class ProductEntity {

    @Id
    private final Integer id;
    @NonNull
    private final String name;
    @Nullable
    private final String unit;
    @Nullable
    @Column("package")
    private final Float inPackage;
    @Nullable
    private final String erpCode;
    @Nullable
    private final String barCode;
    @Nullable
    private final Float weight;
    @Nullable
    private final Integer brandId;
    @Nullable
    private final Integer groupId;
    @NonNull
    private final Instant createdAt;
    @Nullable
    private final Instant deletedAt;
    @Nullable
    private final Instant updatedAt;
    @MappedCollection(idColumn = "product_id")
    private final Set<PriceEntity> prices;

    @PersistenceCreator
    public ProductEntity(Integer id, String name, String unit,
                         Float inPackage, String erpCode, String barCode,
                         Float weight, Integer brandId, Integer groupId,
                         Instant createdAt, Instant updatedAt, Instant deletedAt,
                         Set<PriceEntity> prices) {
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

    public ProductEntity(String name, String unit, Float inPackage, String erpCode,
                   String barCode, Float weight, Set<PriceEntity> prices) {
        this(null, name, unit, inPackage, erpCode, barCode,
                weight, null, null, Instant.now(), null, null, prices);
    }

    public ProductEntity(String name, String unit, Float inPackage, String erpCode,
                   String barCode, Float weight, Integer brandId, Integer groupId) {
        this(null, name, unit, inPackage, erpCode, barCode,
                weight, brandId, groupId, Instant.now(), null, null, new HashSet<>());
    }

    public ProductEntity updateFrom(ProductEntity other) {
        return new ProductEntity(this.id, other.getName(), other.getUnit(),
                other.getInPackage(), other.getErpCode(), other.getBarCode(),
                other.getWeight(), other.getBrandId(), other.getGroupId(),
                this.createdAt, Instant.now(), null,
                this.prices);
    }

    public ProductEntity withGroupId(Integer groupId) {
        return new ProductEntity(id, name, unit, inPackage, erpCode,
                barCode, weight, brandId, groupId, createdAt, updatedAt, deletedAt,
                prices);
    }

    public ProductEntity withBrandId(Integer brandId) {
        return new ProductEntity(id, name, unit, inPackage, erpCode,
                barCode, weight, brandId, groupId, createdAt, updatedAt, deletedAt,
                prices);
    }

    public float getPrice(PriceType priceType) {
        return prices == null ?
                0f :
                prices.stream()
                        .filter(p -> p.getType() == priceType)
                        .findFirst()
                        .orElse(new PriceEntity(0f))
                        .getPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductEntity product)) return false;
        return Objects.equals(id, product.id) && Objects.equals(erpCode, product.erpCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, erpCode);
    }
}
