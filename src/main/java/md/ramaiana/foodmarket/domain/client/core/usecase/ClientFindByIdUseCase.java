package md.ramaiana.foodmarket.domain.client.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for finding a client by id.
 */
@UseCase
@RequiredArgsConstructor
public class ClientFindByIdUseCase {

  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ClientEntity execute(@NonNull AggregateReference<ClientEntity, Integer> client) {
    return clientRepository.findById(client.getId())
        .orElseThrow(() -> new NotFoundException(String.format("Client with id '%s' not found", client.getId())));
  }
}
