package md.ramaiana.foodmarket.domain.client.core.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;

/**
 * Client response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

  private Integer id;

  @NonNull
  private String name;

  @NonNull
  private String idno;

  public ClientResponse(@NonNull ClientEntity entity) {
    this.id = entity.getId();
    this.name = entity.getName();
    this.idno = entity.getIdno();
  }
}
