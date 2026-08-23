package md.ramaiana.foodmarket.domain.client.core.usecase;

import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for searching clients. Serves both the paged admin listing and the single-client
 * lookup behind /client/findByIdno.
 */
@UseCase
@RequiredArgsConstructor
public class ClientSearchUseCase {

  /**
   * Sort is a free-text request parameter, so it is constrained to an explicit set of
   * entity properties rather than passed straight to the persistence layer.
   */
  private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "name", "idno", "createdAt");

  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public PagedResponse<ClientResponse> execute(@NonNull ClientSearchCriteria criteria) {
    if (!SORTABLE_PROPERTIES.contains(criteria.sortColumn())) {
      throw new BadRequestException(String.format("Unknown sort column '%s'", criteria.sortColumn()));
    }

    Pageable pageable = PageRequest.of(criteria.pageNo(), criteria.pageSize(),
        Sort.by(criteria.sortDirection(), criteria.sortColumn()));

    Page<ClientEntity> page = clientRepository.search(criteria.nameLike(), criteria.idno(), pageable);

    return new PagedResponse<>(page.map(ClientResponse::new));
  }

  /**
   * Find the single client carrying this idno.
   * <p>
   * DEVIATION: a second public method on a use case. It is a thin wrapper over {@link #execute}
   * rather than a second query path, kept here so the not-found rule stays in the use case
   * instead of leaking into the controller.
   *
   * @throws NotFoundException if no live client has that idno.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ClientResponse executeByIdno(@NonNull String idno) {
    return execute(ClientSearchCriteria.byIdno(idno)).items().stream()
        .findFirst()
        .orElseThrow(() -> new NotFoundException(String.format("Client with idno '%s' not found", idno)));
  }
}
