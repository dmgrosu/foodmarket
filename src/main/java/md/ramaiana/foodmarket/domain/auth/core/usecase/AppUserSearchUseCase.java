package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.response.AppUserResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Use case for the admin user listing.
 */
@UseCase
@RequiredArgsConstructor
public class AppUserSearchUseCase {

  /**
   * Sort is a free-text request parameter, so it is constrained to an explicit set of
   * entity properties rather than passed straight to the persistence layer.
   */
  private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "email", "state", "createdAt");

  private final AppUserRepository appUserRepository;
  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public PagedResponse<AppUserResponse> execute(@NonNull AppUserSearchCriteria criteria) {
    if (!SORTABLE_PROPERTIES.contains(criteria.sortColumn())) {
      throw new BadRequestException(String.format("Unknown sort column '%s'", criteria.sortColumn()));
    }

    Pageable pageable = PageRequest.of(criteria.pageNo(), criteria.pageSize(),
        Sort.by(criteria.sortDirection(), criteria.sortColumn()));

    Page<AppUserEntity> page = appUserRepository.search(criteria.emailLike(), criteria.state(), pageable);

    Map<Integer, String> clientNamesById = resolveClientNames(page);

    return new PagedResponse<>(page.map(user -> new AppUserResponse(
        user, user.hasClient() ? clientNamesById.get(user.getClient().getId()) : null)));
  }

  /**
   * Resolve every linked client's name in one query rather than one {@code findById} per row.
   */
  @NonNull
  private Map<Integer, String> resolveClientNames(@NonNull Page<AppUserEntity> page) {
    Set<Integer> clientIds = page.getContent().stream()
        .filter(AppUserEntity::hasClient)
        .map(AppUserEntity::getClient)
        .map(AggregateReference::getId)
        .collect(Collectors.toSet());

    if (clientIds.isEmpty()) {
      return Map.of();
    }

    return StreamSupport.stream(clientRepository.findAllById(clientIds).spliterator(), false)
        .collect(Collectors.toMap(ClientEntity::getId, ClientEntity::getName));
  }
}
