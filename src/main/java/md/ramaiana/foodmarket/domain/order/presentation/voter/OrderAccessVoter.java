package md.ramaiana.foodmarket.domain.order.presentation.voter;

import md.ramaiana.foodmarket.shared.util.abstraction.voter.AccessVoter;
import org.springframework.stereotype.Component;

/**
 * Order Access Voter.
 * <p>
 * These assert authentication only. Whether the caller may touch a <em>particular</em> order is not
 * decidable here — it depends on the order, which the voter never sees — so that check lives in
 * {@code OrderLoader}, which is the only way an order is fetched for an HTTP caller.
 */
@Component
public class OrderAccessVoter extends AccessVoter {

  /**
   * Assert that the current user can read their cart.
   */
  public void assertCanGetCart() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can add a product to their cart.
   */
  public void assertCanAddProduct() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can change the quantity of a product in their cart.
   */
  public void assertCanUpdateProduct() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can remove a product from their cart.
   */
  public void assertCanDeleteProduct() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can empty their cart.
   */
  public void assertCanClearCart() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can place their cart as an order.
   */
  public void assertCanPlaceOrder() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can get an order by ID.
   */
  public void assertCanGetById() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can get orders by period.
   */
  public void assertCanGetOrdersByPeriod() {
    assertUserIsAuthenticated();
  }

  /**
   * Assert that the current user can delete an order.
   */
  public void assertCanDelete() {
    assertUserIsAuthenticated();
  }
}
