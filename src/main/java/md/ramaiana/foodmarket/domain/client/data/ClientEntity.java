package md.ramaiana.foodmarket.domain.client.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Client Entity.
 */
@Getter
@Setter
@Entity
@Table(name = "client")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientEntity {

  @NonNull
  @Setter(AccessLevel.NONE)
  private final UUID uuid = UUID.randomUUID();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @NonNull
  private String name;

  @NonNull
  @Column(unique = true)
  private String idno;

  @Nullable
  private Instant deletedAt;
}
