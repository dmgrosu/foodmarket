package md.ramaiana.foodmarket.domain.client.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClientFindByIdUseCaseTest extends BaseTest {

  @Autowired
  private ClientFindByIdUseCase clientFindByIdUseCase;

  @Test
  void whenClientExists_thenReturnsClient() {
    ClientEntity client = testClientService.create("ACME Corp", "1234567890123");

    ClientEntity found = clientFindByIdUseCase.execute(client.getId());

    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(client.getId());
    assertThat(found.getName()).isEqualTo("ACME Corp");
    assertThat(found.getIdno()).isEqualTo("1234567890123");
  }

  @Test
  void whenClientNotFound_thenThrowsNotFound() {
    assertThatThrownBy(() -> clientFindByIdUseCase.execute(999))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found");
  }
}
