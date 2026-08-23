package md.ramaiana.foodmarket.domain.client.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Admin Client Access Voter.
 */
@Component
public class AdminClientAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can search clients as an administrator.
   */
  public void assertCanSearch() {
    assertUserIsAdmin();
  }
}
