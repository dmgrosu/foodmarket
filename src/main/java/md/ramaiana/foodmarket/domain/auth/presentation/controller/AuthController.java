package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication management endpoints")
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
  @Operation(
      operationId = "login",
      summary = "User login",
      description = "Authenticate a user with email and password"
  )
  public AuthResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
    accessVoter.assertCanLogin();
    return authLoginUseCase.execute(request);
  }

  /**
   * Register.
   */
  @PostMapping("/register")
  @Operation(
      operationId = "register",
      summary = "User registration",
      description = "Register a new user account"
  )
  public AuthResponse register(@Valid @RequestBody @NonNull RegisterRequest request) {
    accessVoter.assertCanRegister();
    return authRegisterUseCase.execute(request);
  }
}