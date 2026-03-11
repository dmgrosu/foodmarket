package md.ramaiana.foodmarket.domain.client.data;

import lombok.Getter;
import md.ramaiana.foodmarket.shared.enums.AddressType;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("client_addresses")
public class ClientAddressEntity {

    private final AddressType type;
    private final String fullAddress;
    private final String description;

    @PersistenceCreator
    public ClientAddressEntity(AddressType type,
                               String fullAddress,
                               String description) {
        this.type = type;
        this.fullAddress = fullAddress;
        this.description = description;
    }
}
