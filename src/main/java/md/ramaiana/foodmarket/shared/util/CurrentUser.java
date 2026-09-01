package md.ramaiana.foodmarket.shared.util;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The single place that unwraps the authenticated {@link AppUserEntity} out of the Spring Security
 * context.
 * <p>
 * Static rather than a bean because {@code AccessVoter} subclasses are constructed directly in unit
 * tests, with no application context to inject into. Use cases reach the same logic through
 * {@link CurrentUserProvider}, which is injectable and therefore mockable.
 */
public final class CurrentUser {

  private CurrentUser() {
  }

  /**
   * The authenticated user behind the current request.
   *
   * @throws UnauthorizedException if nobody is authenticated, or the principal is not one of ours.
   */
  public static AppUserEntity require() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("User is not authenticated");
    }

    Object principal = authentication.getPrincipal();

    // Handle anonymous users
    if (principal instanceof String && principal.equals("anonymousUser")) {
      throw new UnauthorizedException("User is not authenticated");
    }

    // The principal should be AppUserEntity based on JwtGetAuthenticationUseCase
    if (!(principal instanceof AppUserEntity user)) {
      throw new UnauthorizedException("Invalid authentication principal");
    }

    return user;
  }
}
