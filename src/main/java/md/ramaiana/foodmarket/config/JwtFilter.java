package md.ramaiana.foodmarket.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.auth.core.usecase.JwtGetAuthenticationUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter.
 * Extracts JWT token from Authorization header and authenticates the request.
 *
 * @author Dmitri Grosu, 2/6/21
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;

  private final JwtGetAuthenticationUseCase jwtGetAuthenticationUseCase;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws IOException, ServletException {

    // Extract token from request
    String token = extractTokenFromRequest(request);

    // If no token is present, continue without authentication
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Validate token and get authentication
      Authentication authentication = jwtGetAuthenticationUseCase.execute(token);

      if (authentication != null && authentication.isAuthenticated()) {
        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Successfully authenticated user: {}", authentication.getName());

        // Continue with the filter chain
        filterChain.doFilter(request, response);
      } else {
        // Invalid or expired token
        log.debug("Invalid authentication token");
        handleAuthenticationFailure(response, "Invalid or expired token");
      }
    } catch (Exception e) {
      // Token validation failed
      log.debug("JWT authentication failed: {}", e.getMessage());
      handleAuthenticationFailure(response, "Authentication failed: " + e.getMessage());
    } finally {
      // Clean up security context if authentication failed
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        SecurityContextHolder.clearContext();
      }
    }
  }

  /**
   * Extract JWT token from the Authorization header.
   *
   * @param request The HTTP request.
   * @return The JWT token, or null if not present.
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX_LENGTH);
    }

    return null;
  }

  /**
   * Handle authentication failure by sending 401 Unauthorized response.
   *
   * @param response The HTTP response.
   * @param message  The error message.
   * @throws IOException If an I/O error occurs.
   */
  private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String jsonResponse = String.format(
        "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
        message
    );

    response.getWriter().write(jsonResponse);
  }

  /**
   * Skip filter for public endpoints.
   * This is optional but can improve performance by skipping JWT processing for public endpoints.
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();

    // Skip JWT filter for public endpoints
    return path.startsWith("/auth/register") ||
        path.startsWith("/auth/login");
  }
}