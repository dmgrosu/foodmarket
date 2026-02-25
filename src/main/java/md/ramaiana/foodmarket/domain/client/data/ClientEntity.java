package md.ramaiana.foodmarket.domain.client.data;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Client Entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("client")
public class ClientEntity {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  @NonNull
  private String name;
  @NonNull
  private String idno;
  @Nullable
  private Instant deletedAt;
}
