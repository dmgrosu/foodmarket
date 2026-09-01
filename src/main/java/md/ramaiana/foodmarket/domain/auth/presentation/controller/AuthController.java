package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.handler.AuthRegisterRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.handler.PasswordResetInitiateRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.handler.RegistrationConfirmationResendRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordChangeRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordResetCompleteRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordResetInitiateRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.ProfileUpdateRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.LoginRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmationResendRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.AuthResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.PasswordResetInitiateResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.ProfileResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationConfirmResponse;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AuthLoginUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.PasswordChangeUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.PasswordResetCompleteUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.ProfileFindUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.ProfileUpdateUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationConfirmUseCase;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.presentation.voter.AuthAccessVoter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final PasswordResetCompleteUseCase passwordResetCompleteUseCase;
  private final ProfileFindUseCase profileFindUseCase;
  private final ProfileUpdateUseCase profileUpdateUseCase;
  private final PasswordChangeUseCase passwordChangeUseCase;

  // Request handlers
  private final AuthRegisterRequestHandler authRegisterRequestHandler;
  private final RegistrationConfirmationResendRequestHandler authRegistrationConfirmationResendRequestHandler;
  private final PasswordResetInitiateRequestHandler passwordResetInitiateRequestHandler;

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

  /**
   * Request a password reset link. Answers the same way whether or not the address is registered.
   */
  @PostMapping("/forgotPassword")
  public PasswordResetInitiateResponse forgotPassword(
      @Valid @RequestBody @NonNull PasswordResetInitiateRequest request) {
    accessVoter.assertCanRequestPasswordReset();
    return passwordResetInitiateRequestHandler.handle(request);
  }

  /**
   * Set a new password using the token from a reset email.
   */
  @PostMapping("/resetPassword")
  public void resetPassword(@Valid @RequestBody @NonNull PasswordResetCompleteRequest request) {
    accessVoter.assertCanResetPassword();
    passwordResetCompleteUseCase.execute(request);
  }

  /**
   * Read the signed-in user's own profile.
   */
  @GetMapping("/profile")
  public ProfileResponse profile(@AuthenticationPrincipal AppUserEntity currentUser) {
    accessVoter.assertCanGetProfile();
    return profileFindUseCase.execute(currentUser);
  }

  /**
   * Update the signed-in user's own profile.
   */
  @PutMapping("/updateProfile")
  public ProfileResponse updateProfile(@AuthenticationPrincipal AppUserEntity currentUser,
                                       @Valid @RequestBody @NonNull ProfileUpdateRequest request) {
    accessVoter.assertCanUpdateProfile();
    return profileUpdateUseCase.execute(currentUser, request);
  }

  /**
   * Change the signed-in user's own password.
   */
  @PutMapping("/changePassword")
  public void changePassword(@AuthenticationPrincipal AppUserEntity currentUser,
                             @Valid @RequestBody @NonNull PasswordChangeRequest request) {
    accessVoter.assertCanChangePassword();
    passwordChangeUseCase.execute(currentUser, request);
  }
}
