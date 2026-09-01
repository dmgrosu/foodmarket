package md.ramaiana.foodmarket.domain.client.core.usecase;

import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ClientFindByIdnoUseCaseTest {

  @Mock
  ClientRepository clientRepository;

  @InjectMocks
  ClientFindByIdnoUseCase useCase;

  @Test
  void execute_should_return_the_matching_client() {
    ClientEntity entity = new ClientEntity(3, "Linella", "1003600011111", "info@linella.md",
        "00000003", Instant.now(), null, Set.of(), Set.of());
    when(clientRepository.findByIdnoAndDeletedAtIsNull("1003600011111")).thenReturn(Optional.of(entity));

    ClientResponse response = useCase.execute("1003600011111");

    assertThat(response.getId()).isEqualTo(3);
    assertThat(response.getIdno()).isEqualTo("1003600011111");
  }

  @Test
  void execute_should_throw_not_found_when_no_client_matches() {
    when(clientRepository.findByIdnoAndDeletedAtIsNull("0000000000000")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("0000000000000"))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("0000000000000");
  }
}
