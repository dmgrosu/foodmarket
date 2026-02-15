package md.ramaiana.foodmarket.domain.auth.core.usecase;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.util.Date;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.beans.factory.annotation.Value;

/**
 * Use case for creating JWT tokens.
 */
@UseCase
@RequiredArgsConstructor
public class JwtCreateTokenUseCase {

  @Value("${jwt.token.secret}")
  private String tokenSecret;

  @Value("${jwt.token.validity}")
  private Integer tokenValidity;

  /**
   * Execute the use case.
   */
  public String execute(@NonNull AppUserEntity appUser) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
      Date now = new Date();
      return "Bearer " + JWT.create()
          .withClaim("username", appUser.getUsername())
          .withClaim("userId", appUser.getId().toString())
          .withClaim("createdAt", now)
          .withExpiresAt(new Date(now.getTime() + tokenValidity))
          .sign(algorithm);
    } catch (JWTCreationException exception) {
      exception.printStackTrace();
    }
    return null;
  }

  /**
   * Get token validity in seconds.
   */
  public int getTokenValidityInSeconds() {
    return tokenValidity / 1000;
  }
}
