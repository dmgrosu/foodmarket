package md.ramaiana.foodmarket.domain.auth.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthRegisterUseCaseTest extends BaseTest {

  @Autowired
  private AuthRegisterUseCase authRegisterUseCase;

  @Autowired
  private AppUserRepository appUserRepository;

  @Test
  void whenValidRequest_thenCreatesUserAndReturnsToken() {
    AuthResponse response = authRegisterUseCase.execute(
        new RegisterRequest("newuser@test.com", "securePass123", null)
    );

    assertThat(response).isNotNull();
    assertThat(response.getToken()).startsWith("Bearer ");
    assertThat(response.getTokenTtl()).isGreaterThan(0);
    assertThat(response.getUser().getEmail()).isEqualTo("newuser@test.com");
    assertThat(response.getUser().getClient()).isNull();
  }

  @Test
  void whenDuplicateEmail_thenThrowsBadRequest() {
    testAppUserService.create("existing@test.com", Role.USER);

    assertThatThrownBy(() -> authRegisterUseCase.execute(
        new RegisterRequest("existing@test.com", "password123", null)
    )).isInstanceOf(BadRequestException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void whenRegistered_thenUserHasRoleUser() {
    authRegisterUseCase.execute(
        new RegisterRequest("rolecheck@test.com", "password123", null)
    );

    AppUserEntity savedUser = appUserRepository.findByEmail("rolecheck@test.com")
        .orElseThrow();

    assertThat(savedUser.getRoles()).containsExactly(Role.USER);
  }
}
