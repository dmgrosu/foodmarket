package md.ramaiana.foodmarket.domain.client.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Set;

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
    @MappedCollection(idColumn = "client_id")
    private final Set<ClientAddressEntity> addresses;
    @NonNull
    @MappedCollection(idColumn = "client_id")
    private final Set<ClientPhoneEntity> phones;

    @PersistenceCreator
    public ClientEntity(Integer id, @NonNull String name, @NonNull String idno, String email,
                        @NonNull Instant createdAt, Instant deletedAt,
                        @NonNull Set<ClientAddressEntity> addresses,
                        @NonNull Set<ClientPhoneEntity> phones) {
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
                        @NonNull Set<ClientAddressEntity> addresses, @NonNull Set<ClientPhoneEntity> phones) {
        this(null, name, idno, email, Instant.now(), null, addresses, phones);
    }

}
