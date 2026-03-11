package md.ramaiana.foodmarket.domain.client.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;

/**
 * Client Entity.
 */
@Getter
@Table("client")
public class ClientEntity {

    @Id
    private final Integer id;
    @NonNull
    private final String name;
    @NonNull
    private final String idno;
    private final String email;
    @NonNull
    private final Instant createdAt;
    private final Instant deletedAt;
    @NonNull
    private final List<ClientAddressEntity> addresses;
    @NonNull
    private final List<ClientPhoneEntity> phones;

    @PersistenceCreator
    public ClientEntity(Integer id, @NonNull String name, @NonNull String idno, String email,
                        @NonNull Instant createdAt, Instant deletedAt,
                        @NonNull List<ClientAddressEntity> addresses,
                        @NonNull List<ClientPhoneEntity> phones) {
        this.id = id;
        this.name = name;
        this.idno = idno;
        this.email = email;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.addresses = addresses;
        this.phones = phones;
    }

    public ClientEntity(@NonNull String name, @NonNull String idno, String email,
                        @NonNull List<ClientAddressEntity> addresses, @NonNull List<ClientPhoneEntity> phones) {
        this(null, name, idno, email, Instant.now(), null, addresses, phones);
    }

}
