package md.ramaiana.foodmarket.domain.auth.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Auth Access Voter.
 */
@Component
public class AuthAccessVoter extends AccessVoter {

  /**
   * Assert that the user can login.
   * Login is public, no authentication required.
   */
  public void assertCanLogin() {
    // Public endpoint - no authentication required
  }

  /**
   * Assert that the user can register.
   * Registration is public, no authentication required.
   */
  public void assertCanRegister() {
    // Public endpoint - no authentication required
  }

  /**
   * Assert that the user can confirm their email.
   * Confirming an email is public, no authentication required.
   */
  public void assertCanConfirmEmail() {
    // Public endpoint - no authentication required
  }

  /**
   * Assert that the user can request a new confirmation email.
   * Resending a confirmation is public, no authentication required.
   */
  public void assertCanResendConfirmation() {
    // Public endpoint - no authentication required
  }
}
