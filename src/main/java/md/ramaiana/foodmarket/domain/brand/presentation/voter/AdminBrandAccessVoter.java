package md.ramaiana.foodmarket.domain.brand.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Admin Brand Access Voter.
 */
@Component
public class AdminBrandAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can search brands as an administrator.
   */
  public void assertCanSearch() {
    assertUserIsAdmin();
  }
}
