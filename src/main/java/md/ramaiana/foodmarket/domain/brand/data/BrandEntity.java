package md.ramaiana.foodmarket.domain.brand.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Table("brand")
public class BrandEntity {

    private final UUID uuid = UUID.randomUUID();
    @Id
    private final Integer id;
    @NonNull
    private final String name;
    @NonNull
    private final String erpCode;

    @PersistenceCreator
    public BrandEntity(Integer id, @NonNull String name,
                       @NonNull String erpCode) {
        this.id = id;
        this.name = name;
        this.erpCode = erpCode;
    }

    public BrandEntity(@NonNull String name, @NonNull String erpCode) {
        this(null, name, erpCode);
    }
}
