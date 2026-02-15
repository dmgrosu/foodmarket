package md.ramaiana.foodmarket.shared.util.abstraction.voter;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base class for access voters that determine if a user can perform certain actions.
 */
public abstract class AccessVoter {

  /**
   * Get the current authenticated user from the security context.
   *
   * @throws UnauthorizedException if no user is authenticated.
   */
  protected void getCurrentUser() {
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
    if (!(principal instanceof AppUserEntity)) {
      throw new UnauthorizedException("Invalid authentication principal");
    }

  }

  /**
   * Assert that a user is authenticated.
   *
   * @throws UnauthorizedException if the user is not authenticated.
   */
  protected void assertUserIsAuthenticated() {
    getCurrentUser();
  }
}