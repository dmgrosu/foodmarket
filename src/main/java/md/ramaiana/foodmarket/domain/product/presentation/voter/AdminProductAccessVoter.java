package md.ramaiana.foodmarket.domain.product.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Admin Product Access Voter.
 */
@Component
public class AdminProductAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can search products as an administrator.
   */
  public void assertCanSearch() {
    assertUserIsAdmin();
  }
}
