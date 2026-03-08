package md.ramaiana.foodmarket.domain.brand.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Table("brand")
public class BrandEntity {

    @Id
    private final Integer id;
    @NonNull
    private final String name;
    @NonNull
    private final String erpCode;
    @NonNull
    private final Instant createdAt;
    private final Instant deletedAt;

    @PersistenceCreator
    public BrandEntity(Integer id, @NonNull String name,
                       @NonNull String erpCode, @NonNull Instant createdAt,
                       Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.erpCode = erpCode;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public BrandEntity(@NonNull String name, @NonNull String erpCode) {
        this(null, name, erpCode, Instant.now(), null);
    }
}
