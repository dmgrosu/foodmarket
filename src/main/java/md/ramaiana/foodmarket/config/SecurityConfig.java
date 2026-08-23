package md.ramaiana.foodmarket.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.usecase.JwtGetAuthenticationUseCase;
import md.ramaiana.foodmarket.domain.auth.presentation.service.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration.
 *
 * @author Dmitri Grosu, 2/6/21
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtGetAuthenticationUseCase jwtGetAuthenticationUseCase;
  private final AppUserDetailsService appUserDetailsService;

  /**
   * Configure security filter chain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(request -> request
            // Public endpoints
            .requestMatchers(
                "/auth/register",
                "/auth/login",
                "/client/findByIdno",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .sessionManagement(manager ->
            manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(
            new JwtFilter(jwtGetAuthenticationUseCase),
            UsernamePasswordAuthenticationFilter.class
        )
        .build();
  }

  /**
   * Authentication manager bean.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
    return configuration.getAuthenticationManager();
  }

  /**
   * Password encoder bean.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Authentication provider bean.
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(appUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * CORS configuration source.
   * <p>
   * Note: In production, you should specify exact allowed origins instead of "*".
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // WARNING: In production, replace "*" with your actual frontend URLs
    // Example: configuration.setAllowedOrigins(List.of("https://yourdomain.com", "http://localhost:3000"));
    configuration.setAllowedOriginPatterns(List.of("*"));

    // Allowed HTTP methods
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Allowed headers
    configuration.setAllowedHeaders(List.of("*"));

    // Expose headers (important for custom headers like X-Total-Count)
    configuration.setExposedHeaders(List.of(
        "Authorization",
        "X-Total-Count",
        "X-Page-Number",
        "X-Page-Size"
    ));

    // Allow credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);

    // Cache preflight response for 1 hour
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}