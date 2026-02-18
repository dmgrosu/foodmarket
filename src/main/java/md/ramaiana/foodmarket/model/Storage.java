package md.ramaiana.foodmarket.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("storages")
@Getter
public class Storage {
    @Id
    private final Integer id;
    @Column("name")
    private final String name;
    @Column("erp_code")
    private final String erpCode;

    @PersistenceCreator
    public Storage(Integer id, String name, String erpCode) {
        this.id = id;
        this.name = name;
        this.erpCode = erpCode;
    }
}
