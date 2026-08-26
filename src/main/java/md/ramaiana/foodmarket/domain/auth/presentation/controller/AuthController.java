package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.handler.AuthRegisterRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.handler.RegistrationConfirmationResendRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmationResendRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationConfirmResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AuthLoginUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationConfirmUseCase;
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
  private final RegistrationConfirmUseCase registrationConfirmUseCase;

  // Request handlers
  private final AuthRegisterRequestHandler authRegisterRequestHandler;
  private final RegistrationConfirmationResendRequestHandler authRegistrationConfirmationResendRequestHandler;

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
  public RegistrationResponse register(@Valid @RequestBody @NonNull RegisterRequest request) {
    accessVoter.assertCanRegister();
    return authRegisterRequestHandler.handle(request);
  }

  /**
   * Confirm a registration email via its magic-link token.
   */
  @PostMapping("/confirmEmail")
  public RegistrationConfirmResponse confirmEmail(@Valid @RequestBody @NonNull RegistrationConfirmRequest request) {
    accessVoter.assertCanConfirmEmail();
    return registrationConfirmUseCase.execute(request);
  }

  /**
   * Resend a registration confirmation email.
   */
  @PostMapping("/resendConfirmation")
  public RegistrationResponse resendConfirmation(@Valid @RequestBody @NonNull RegistrationConfirmationResendRequest request) {
    accessVoter.assertCanResendConfirmation();
    return authRegistrationConfirmationResendRequestHandler.handle(request);
  }
}
