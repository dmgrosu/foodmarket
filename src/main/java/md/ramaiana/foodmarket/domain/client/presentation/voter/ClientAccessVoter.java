package md.ramaiana.foodmarket.domain.client.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Client Access Voter.
 */
@Component
public class ClientAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can find a client by ID number.
   */
  public void assertCanFindByIdno() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can search clients. Administrators only.
   */
  public void assertCanSearch() {
    assertUserIsAdmin();
  }
}
