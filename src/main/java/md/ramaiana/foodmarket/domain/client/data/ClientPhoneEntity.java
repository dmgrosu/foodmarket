package md.ramaiana.foodmarket.domain.client.data;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("client_phones")
public class ClientPhoneEntity {

    private final String number;
    private final String name;

    @PersistenceCreator
    public ClientPhoneEntity(String number, String name) {
        this.number = number;
        this.name = name;
    }
}
