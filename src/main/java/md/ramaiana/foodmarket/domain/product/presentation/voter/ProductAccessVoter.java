package md.ramaiana.foodmarket.domain.product.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Product Access Voter.
 */
@Component
public class ProductAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can list product groups.
   */
  public void assertCanListGroups() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can list products.
   */
  public void assertCanListProducts() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can search products.
   */
  public void assertCanSearch() {
    assertUserIsAuthenticated();
  }
}