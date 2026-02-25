package md.ramaiana.foodmarket.domain.auth.core.usecase;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.beans.factory.annotation.Value;

/**
 * Use case for verifying JWT tokens and extracting user data.
 */
@UseCase
@RequiredArgsConstructor
public class JwtVerifyTokenUseCase {

  @Value("${jwt.token.secret}")
  private String tokenSecret;

  /**
   * Execute the use case.
   */
  public Map<String, String> execute(@NonNull String token) {
    Map<String, String> result = new HashMap<>();
    try {
      Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
      JWTVerifier verifier = JWT.require(algorithm).build();
      DecodedJWT jwt = verifier.verify(token);
      result.put("email", jwt.getClaim("username").asString());
      result.put("id", jwt.getClaim("userId").asString());
      return result;
    } catch (JWTVerificationException exception) {
      exception.printStackTrace();
      return null;
    }
  }
}
