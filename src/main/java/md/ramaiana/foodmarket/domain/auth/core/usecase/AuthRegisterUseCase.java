package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for user registration.
 */
@UseCase
@RequiredArgsConstructor
public class AuthRegisterUseCase {

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtCreateTokenUseCase jwtCreateTokenUseCase;
  private final ClientFindByIdUseCase clientFindByIdUseCase;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public AuthResponse execute(@NonNull RegisterRequest request) {
    // Check if user already exists
    if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new BadRequestException(String.format("User with email '%s' already exists", request.getEmail()));
    }

    // Create new user
    AppUserEntity user = new AppUserEntity(
        request.getEmail(),
        passwordEncoder.encode(request.getPassword())
    );
    user.addRole(Role.USER);

    // Save user
    AppUserEntity savedUser = appUserRepository.save(user);

    // Generate token
    String token = jwtCreateTokenUseCase.execute(savedUser);
    int tokenTtl = jwtCreateTokenUseCase.getTokenValidityInSeconds();

    // Get client if exists
    ClientResponse clientResponse = null;
    if (savedUser.hasClient()) {
      ClientEntity client = clientFindByIdUseCase.execute(savedUser.getClientId());
      clientResponse = new ClientResponse(client);
    }

    return new AuthResponse(
        new AuthResponse.UserResponse(savedUser, clientResponse),
        token,
        tokenTtl
    );
  }
}
