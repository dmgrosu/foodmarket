package md.ramaiana.foodmarket.domain.auth.core.usecase;

import md.ramaiana.foodmarket.domain.auth.core.response.AppUserResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppUserSearchUseCaseTest {

  @Mock
  AppUserRepository appUserRepository;
  @Mock
  ClientRepository clientRepository;

  @InjectMocks
  AppUserSearchUseCase useCase;

  private static AppUserEntity user(Integer id, String email, UserState state, Integer clientId) {
    AggregateReference<ClientEntity, Integer> client =
        clientId == null ? null : AggregateReference.to(clientId);
    return new AppUserEntity(id, email, "hash", Instant.now(), state, Language.RU, new HashSet<>(), client);
  }

  @Test
  void execute_should_reject_sort_column_outside_the_whitelist() {
    assertThatThrownBy(() -> useCase.execute(
        new AppUserSearchCriteria(null, null, 0, 25, "passwd", Sort.Direction.ASC)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("passwd");

    verifyNoInteractions(appUserRepository, clientRepository);
  }

  @Test
  void execute_should_pass_filters_through_and_resolve_client_names_in_one_call() {
    AppUserEntity withClient = user(1, "a@example.com", UserState.CONFIRMED, 42);
    AppUserEntity withoutClient = user(2, "b@example.com", UserState.CONFIRMED, null);

    when(appUserRepository.search(eq("example"), eq(UserState.CONFIRMED), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(withClient, withoutClient), PageRequest.of(0, 25), 2));
    when(clientRepository.findAllById(anyIterable()))
        .thenReturn(List.of(new ClientEntity(42, "Acme", "1000000000001", null, Instant.now(), null, Set.of(), Set.of())));

    PagedResponse<AppUserResponse> response = useCase.execute(
        new AppUserSearchCriteria("example", UserState.CONFIRMED, 0, 25, "email", Sort.Direction.ASC));

    assertThat(response.items()).hasSize(2);
    assertThat(response.items().get(0).clientName()).isEqualTo("Acme");
    assertThat(response.items().get(1).clientName()).isNull();

    // Exactly one batched lookup, never one per row.
    verify(clientRepository).findAllById(anyIterable());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(appUserRepository).search(eq("example"), eq(UserState.CONFIRMED), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(25);
  }

  @Test
  void execute_should_skip_the_client_lookup_when_no_row_has_a_client() {
    AppUserEntity withoutClient = user(3, "c@example.com", UserState.PENDING_CONFIRMATION, null);
    when(appUserRepository.search(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(withoutClient), PageRequest.of(0, 25), 1));

    PagedResponse<AppUserResponse> response = useCase.execute(
        new AppUserSearchCriteria(null, null, 0, 25, "email", Sort.Direction.ASC));

    assertThat(response.items().getFirst().clientName()).isNull();
    verify(clientRepository, never()).findAllById(anyIterable());
  }
}
