package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
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
import java.util.Optional;
import java.util.Set;

/**
 * Product Entity.
 */
@Getter
@Table("product")
public class ProductEntity {

    @Id
    @EqualsAndHashCode.Include
    private final Integer id;
    @NonNull
    @EqualsAndHashCode.Include
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
    public ProductEntity(Integer id, @NonNull String name, @Nullable String unit,
                         @Nullable Float inPackage, @Nullable String erpCode, @Nullable String barCode,
                         @Nullable Float weight, @Nullable Integer brandId, @Nullable Integer groupId,
                         @NonNull Instant createdAt, @Nullable Instant updatedAt, @Nullable Instant deletedAt,
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

    /**
     * Copies everything the ERP catalogue owns onto this product, keeping only the identity and
     * creation time of the stored row. Prices come from {@code other} as well - they are part of what
     * the catalogue refreshes, so keeping the stored ones would freeze prices at their first import.
     */
    public ProductEntity updateFrom(ProductEntity other) {
        return new ProductEntity(this.id, other.getName(), other.getUnit(),
                other.getInPackage(), other.getErpCode(), other.getBarCode(),
                other.getWeight(), other.getBrandId(), other.getGroupId(),
                this.createdAt, Instant.now(), null,
                other.getPrices());
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

    /**
     * The price of this product at a storage, on a tier.
     * <p>
     * {@code prices} is keyed by (product, storage, type), so filtering on the type alone returns an
     * arbitrary storage's price. Empty rather than zero when there is no such row: a product with no
     * price on the tier being ordered must not silently land in a cart at nothing.
     */
    @NonNull
    public Optional<Float> findPrice(@NonNull Integer storageId, @NonNull PriceType priceType) {
        return prices == null ?
                Optional.empty() :
                prices.stream()
                        .filter(p -> p.getType() == priceType)
                        .filter(p -> p.getStorage() != null && storageId.equals(p.getStorage().getId()))
                        .findFirst()
                        .map(PriceEntity::getPrice);
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
