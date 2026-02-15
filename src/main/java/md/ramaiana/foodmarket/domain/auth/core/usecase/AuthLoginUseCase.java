package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for user login.
 */
@UseCase
@RequiredArgsConstructor
public class AuthLoginUseCase {

  private final AuthenticationManager authenticationManager;
  private final JwtCreateTokenUseCase jwtCreateTokenUseCase;
  private final ClientFindByIdUseCase clientFindByIdUseCase;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public AuthResponse execute(@NonNull LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    if (!authentication.isAuthenticated()) {
      throw new SecurityException("Authentication failed");
    }

    AppUserEntity user = (AppUserEntity) authentication.getPrincipal();
    if (user == null) {
      throw new SecurityException("Could not find principal for user");
    }
    String token = jwtCreateTokenUseCase.execute(user);
    int tokenTtl = jwtCreateTokenUseCase.getTokenValidityInSeconds();

    ClientResponse clientResponse = null;
    if (user.hasClient()) {
      ClientEntity client = clientFindByIdUseCase.execute(user.getClient().getId());
      clientResponse = new ClientResponse(client);
    }

    return new AuthResponse(
        new AuthResponse.UserResponse(user, clientResponse),
        token,
        tokenTtl
    );
  }
}
