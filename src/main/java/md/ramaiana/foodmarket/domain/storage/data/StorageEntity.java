package md.ramaiana.foodmarket.domain.storage.data;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("storages")
@Getter
public class StorageEntity {
    @Id
    private final Integer id;
    @Column("name")
    private final String name;
    @Column("erp_code")
    private final String erpCode;

    @PersistenceCreator
    public StorageEntity(Integer id, String name, String erpCode) {
        this.id = id;
        this.name = name;
        this.erpCode = erpCode;
    }
}
