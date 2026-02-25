package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Use case for getting Authentication from JWT token.
 */
@UseCase
@RequiredArgsConstructor
public class JwtGetAuthenticationUseCase {

  private final JwtVerifyTokenUseCase jwtVerifyTokenUseCase;
  private final AppUserFindByIdUseCase appUserFindByIdUseCase;

  /**
   * Execute the use case.
   */
  public Authentication execute(@NonNull String token) {
    Map<String, String> userData = jwtVerifyTokenUseCase.execute(token);
    UserDetails userDetails = appUserFindByIdUseCase.execute(Integer.parseInt(userData.get("id")));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }
}
