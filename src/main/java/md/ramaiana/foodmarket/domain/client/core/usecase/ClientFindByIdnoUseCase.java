package md.ramaiana.foodmarket.domain.client.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.util.SpecificationBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for finding a client by idno.
 */
@UseCase
@RequiredArgsConstructor
public class ClientFindByIdnoUseCase {

  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ClientResponse execute(@NonNull String idno) {
    SpecificationBuilder<@NonNull ClientEntity> specification = new SpecificationBuilder<>();

    specification.and(ClientRepository.idnoEquals(idno));
    specification.and(ClientRepository.notDeleted());

    ClientEntity client = clientRepository.findOne(specification.buildOrDefault())
        .orElseThrow(() -> new NotFoundException(String.format("Client with idno '%s' not found", idno)));

    return new ClientResponse(client);
  }
}
