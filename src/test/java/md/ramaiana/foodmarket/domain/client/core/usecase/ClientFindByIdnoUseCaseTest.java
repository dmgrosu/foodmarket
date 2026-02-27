package md.ramaiana.foodmarket.domain.client.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClientFindByIdnoUseCaseTest extends BaseTest {

  @Autowired
  private ClientFindByIdnoUseCase clientFindByIdnoUseCase;

  @Autowired
  private ClientRepository clientRepository;

  @Test
  void whenClientExists_thenReturnsResponse() {
    testClientService.create("ACME Corp", "1234567890123");

    ClientResponse response = clientFindByIdnoUseCase.execute("1234567890123");

    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("ACME Corp");
    assertThat(response.getIdno()).isEqualTo("1234567890123");
  }

  @Test
  void whenClientSoftDeleted_thenThrowsNotFound() {
    ClientEntity client = testClientService.create("Deleted Corp", "9999999999999");
    client.setDeletedAt(java.time.Instant.now());
    clientRepository.save(client);

    assertThatThrownBy(() -> clientFindByIdnoUseCase.execute("9999999999999"))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void whenClientNotFound_thenThrowsNotFound() {
    assertThatThrownBy(() -> clientFindByIdnoUseCase.execute("0000000000000"))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found");
  }
}
