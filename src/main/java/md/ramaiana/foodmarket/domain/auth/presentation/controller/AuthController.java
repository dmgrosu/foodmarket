package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AuthLoginUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AuthRegisterUseCase;
import md.ramaiana.foodmarket.domain.auth.presentation.voter.AuthAccessVoter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  // Access voters
  private final AuthAccessVoter accessVoter;

  // Use cases
  private final AuthLoginUseCase authLoginUseCase;
  private final AuthRegisterUseCase authRegisterUseCase;

  /**
   * Login.
   */
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
    accessVoter.assertCanLogin();
    return authLoginUseCase.execute(request);
  }

  /**
   * Register.
   */
  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody @NonNull RegisterRequest request) {
    accessVoter.assertCanRegister();
    return authRegisterUseCase.execute(request);
  }
}