package md.ramaiana.foodmarket.domain.client.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

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
  @NonNull
  private final Instant createdAt;
  private final Instant deletedAt;

  @PersistenceCreator
  public ClientEntity(Integer id, @NonNull String name, @NonNull String idno,
                      @NonNull Instant createdAt, Instant deletedAt) {
    this.id = id;
    this.name = name;
    this.idno = idno;
    this.createdAt = createdAt;
    this.deletedAt = deletedAt;
  }
}
