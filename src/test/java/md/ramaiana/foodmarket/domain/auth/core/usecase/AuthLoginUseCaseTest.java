package md.ramaiana.foodmarket.domain.auth.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.testservices.TestAppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

class AuthLoginUseCaseTest extends BaseTest {

  @Autowired
  private AuthLoginUseCase authLoginUseCase;

  @Test
  void whenValidCredentials_thenReturnsAuthResponseWithToken() {
    AppUserEntity user = testAppUserService.create();

    AuthResponse response = authLoginUseCase.execute(
        new LoginRequest(user.getEmail(), TestAppUserService.DEFAULT_PASSWORD)
    );

    assertThat(response).isNotNull();
    assertThat(response.getToken()).startsWith("Bearer ");
    assertThat(response.getTokenTtl()).isGreaterThan(0);
    assertThat(response.getUser().getId()).isEqualTo(user.getId());
    assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());
    assertThat(response.getUser().getClient()).isNull();
  }

  @Test
  void whenValidCredentialsWithClient_thenReturnsClientInResponse() {
    ClientEntity client = testClientService.create();
    AppUserEntity user = testAppUserService.createWithClient(client.getId());

    AuthResponse response = authLoginUseCase.execute(
        new LoginRequest(user.getEmail(), TestAppUserService.DEFAULT_PASSWORD)
    );

    assertThat(response.getUser().getClient()).isNotNull();
    assertThat(response.getUser().getClient().getId()).isEqualTo(client.getId());
    assertThat(response.getUser().getClient().getName()).isEqualTo(client.getName());
  }

  @Test
  void whenWrongPassword_thenThrowsBadCredentials() {
    AppUserEntity user = testAppUserService.create();

    assertThatThrownBy(() -> authLoginUseCase.execute(
        new LoginRequest(user.getEmail(), "wrong-password")
    )).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void whenUserNotFound_thenThrowsBadCredentials() {
    assertThatThrownBy(() -> authLoginUseCase.execute(
        new LoginRequest("nonexistent@test.com", "password")
    )).isInstanceOf(BadCredentialsException.class);
  }
}
