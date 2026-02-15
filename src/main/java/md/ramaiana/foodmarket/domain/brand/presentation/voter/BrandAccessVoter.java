package md.ramaiana.foodmarket.domain.brand.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Brand Access Voter.
 */
@Component
public class BrandAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can get all brands.
   */
  public void assertCanGetAll() {
    assertUserIsAuthenticated();
  }
}