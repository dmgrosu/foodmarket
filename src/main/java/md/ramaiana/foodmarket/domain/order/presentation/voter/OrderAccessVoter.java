package md.ramaiana.foodmarket.domain.order.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Order Access Voter.
 */
@Component
public class OrderAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can add a product to an order.
   */
  public void assertCanAddProduct() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can get an order by ID.
   */
  public void assertCanGetById() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can delete an order.
   */
  public void assertCanDelete() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can delete a product from an order.
   */
  public void assertCanDeleteProduct() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can get orders by period.
   */
  public void assertCanGetOrdersByPeriod() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can update an order.
   */
  public void assertCanUpdate() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can place an order.
   */
  public void assertCanPlaceOrder() {
    assertUserIsAuthenticated();
  }
}