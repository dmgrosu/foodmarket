package md.ramaiana.foodmarket.domain.auth.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * User Access Voter. Both handlers are administrator-only.
 */
@Component
public class UserAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can search users.
   */
  public void assertCanSearch() {
    assertUserIsAdmin();
  }

  /**
   * Assert that the current user can activate a user.
   */
  public void assertCanActivate() {
    assertUserIsAdmin();
  }
}
