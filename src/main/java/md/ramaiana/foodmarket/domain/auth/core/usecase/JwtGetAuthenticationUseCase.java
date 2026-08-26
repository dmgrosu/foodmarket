package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.ForbiddenException;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Use case for getting Authentication from JWT token.
 * <p>
 * Re-checks the user's state on every request rather than trusting the token alone. A JWT outlives
 * changes to the account it was issued for, so without this a suspended user would keep full access
 * until their token expired, and the token handed out at email confirmation would let a user who has
 * not been approved yet past every {@code authenticated()} endpoint.
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
    if (userData == null) {
      // JwtVerifyTokenUseCase returns null instead of throwing on a bad signature or expiry.
      throw new UnauthorizedException("Invalid or expired token");
    }

    AppUserEntity user = appUserFindByIdUseCase.execute(Integer.parseInt(userData.get("id")));
    if (!user.isActive()) {
      throw new ForbiddenException(String.format("User '%s' is not active", user.getEmail()));
    }

    return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
  }
}
