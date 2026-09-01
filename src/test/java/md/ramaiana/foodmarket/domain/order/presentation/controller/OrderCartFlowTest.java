package md.ramaiana.foodmarket.domain.order.presentation.controller;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderProductRequest;
import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives the whole cart over HTTP against real repositories.
 * <p>
 * Nothing exercised the {@code order} and {@code order_product} tables before this test, which is
 * how both entities came to declare a {@code uuid} field that no column backs: every write failed,
 * and nothing noticed. Any regression of that kind fails the first {@code addProduct} here.
 */
@TestPropertySource(properties = "dataLoadingDelay=3600000")
class OrderCartFlowTest extends MockedAuthenticationController {

  private static final String OTHER_IDNO = "9000000000002";

  @Autowired
  private ClientRepository clientRepository;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private StorageRepository storageRepository;
  @Autowired
  private JdbcTemplate jdbc;

  private Integer clientId;
  private Integer storageId;
  private Integer colaId;
  private Integer breadId;

  @BeforeEach
  void setUpCatalogue() {
    clientId = clientRepository.save(
        new ClientEntity("Cart Client", "9000000000001", null, "CART-1", Set.of(), Set.of())).getId();
    storageId = storageRepository.save(new StorageEntity(null, "Main", "ST-CART")).getId();

    colaId = saveProduct("Cola", "P-COLA", 1.5f, 10f);
    breadId = saveProduct("Bread", "P-BREAD", 0.5f, 4f);

    authenticateAs(clientId, Role.USER);
  }

  /**
   * JUnit runs a subclass @AfterEach before the superclass one, so the users created by
   * authenticateAs are still referencing these clients at this point. Detaching them first is what
   * lets the client rows go; the base class deletes the users straight after.
   */
  @AfterEach
  void cleanUpCatalogue() {
    jdbc.update("DELETE FROM order_product");
    jdbc.update("DELETE FROM \"order\"");
    jdbc.update("DELETE FROM prices");
    jdbc.update("DELETE FROM product");
    jdbc.update("DELETE FROM storages");
    jdbc.update("UPDATE app_user SET client_id = NULL");
    jdbc.update("DELETE FROM client");
  }

  @Test
  void cart_should_start_empty() throws Exception {
    get("/order/getCart")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.state").value("NEW"))
        .andExpect(jsonPath("$.items").isEmpty());
  }

  @Test
  void adding_a_product_should_open_a_cart_that_survives_a_reload() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.storageId").value(storageId))
        .andExpect(jsonPath("$.priceType").value("LOCAL"))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(colaId))
        .andExpect(jsonPath("$.items[0].productName").value("Cola"))
        .andExpect(jsonPath("$.items[0].quantity").value(2.0))
        .andExpect(jsonPath("$.items[0].price").value(10.0))
        .andExpect(jsonPath("$.items[0].sum").value(20.0))
        .andExpect(jsonPath("$.items[0].weight").value(3.0))
        .andExpect(jsonPath("$.totalSum").value(20.0))
        .andExpect(jsonPath("$.totalWeight").value(3.0));

    // The client keeps no order id, so this is the only way a reloaded page finds its cart again.
    get("/order/getCart")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.totalSum").value(20.0));
  }

  @Test
  void adding_the_same_product_twice_should_increase_the_line_rather_than_add_another() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());

    post("/order/addProduct", addRequest(colaId, 3f))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].quantity").value(5.0))
        .andExpect(jsonPath("$.items[0].sum").value(50.0));
  }

  @Test
  void adding_a_second_product_should_total_both_lines() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());

    post("/order/addProduct", addRequest(breadId, 3f))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2))
        // 2 * 10 + 3 * 4
        .andExpect(jsonPath("$.totalSum").value(32.0))
        // 2 * 1.5 + 3 * 0.5
        .andExpect(jsonPath("$.totalWeight").value(4.5));
  }

  @Test
  void updating_a_product_should_set_the_quantity_outright() throws Exception {
    post("/order/addProduct", addRequest(colaId, 5f)).andExpect(status().isOk());

    put("/order/updateProduct", new UpdateOrderProductRequest(colaId, 2f))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].quantity").value(2.0))
        .andExpect(jsonPath("$.totalSum").value(20.0));
  }

  @Test
  void deleting_a_product_should_remove_its_line() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());
    post("/order/addProduct", addRequest(breadId, 1f)).andExpect(status().isOk());

    delete("/order/deleteProduct/" + colaId)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(breadId))
        .andExpect(jsonPath("$.totalSum").value(4.0));
  }

  @Test
  void clearing_the_cart_should_leave_the_next_add_free_to_open_a_new_one() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());

    delete("/order/clearCart").andExpect(status().isOk());

    get("/order/getCart")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.items").isEmpty());
  }

  @Test
  void placing_should_hand_the_cart_over_and_leave_the_client_with_none() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());

    put("/order/placeOrder")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("PLACED"))
        .andExpect(jsonPath("$.totalSum").value(20.0));

    get("/order/getCart")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").doesNotExist());
  }

  @Test
  void placing_an_empty_cart_should_be_rejected() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());
    delete("/order/deleteProduct/" + colaId).andExpect(status().isOk());

    put("/order/placeOrder").andExpect(status().isBadRequest());
  }

  @Test
  void placing_with_no_cart_at_all_should_be_not_found() throws Exception {
    put("/order/placeOrder").andExpect(status().isNotFound());
  }

  @Test
  void a_placed_order_should_no_longer_be_reachable_through_the_cart() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());
    put("/order/placeOrder").andExpect(status().isOk());

    // requireCart looks for a NEW order, and the placed one is no longer that.
    put("/order/updateProduct", new UpdateOrderProductRequest(colaId, 9f))
        .andExpect(status().isNotFound());
    delete("/order/deleteProduct/" + colaId).andExpect(status().isNotFound());
  }

  @Test
  void a_cart_should_be_locked_to_one_storage_and_tier() throws Exception {
    post("/order/addProduct", addRequest(colaId, 1f)).andExpect(status().isOk());

    Integer otherStorageId = storageRepository.save(new StorageEntity(null, "Other", "ST-OTHER")).getId();
    post("/order/addProduct",
        new AddProductToOrderRequest(otherStorageId, colaId, PriceType.LOCAL, 1f))
        .andExpect(status().isBadRequest());

    post("/order/addProduct",
        new AddProductToOrderRequest(storageId, colaId, PriceType.SALE, 1f))
        .andExpect(status().isBadRequest());
  }

  @Test
  void a_product_with_no_price_on_the_requested_tier_should_be_rejected() throws Exception {
    // Cola is priced LOCAL only, so an empty cart asking for SALE has no price to use.
    post("/order/addProduct", new AddProductToOrderRequest(storageId, colaId, PriceType.SALE, 1f))
        .andExpect(status().isBadRequest());
  }

  @Test
  void another_clients_order_should_not_be_readable() throws Exception {
    post("/order/addProduct", addRequest(colaId, 2f)).andExpect(status().isOk());
    put("/order/placeOrder").andExpect(status().isOk());
    Integer orderId = jdbc.queryForObject("SELECT MAX(id) FROM \"order\"", Integer.class);

    get("/order/getById/" + orderId).andExpect(status().isOk());

    Integer otherClientId = clientRepository.save(
        new ClientEntity("Other Client", OTHER_IDNO, null, "CART-2", Set.of(), Set.of())).getId();
    authenticateAs(otherClientId, Role.USER);

    // Not 403: telling a caller an order they cannot see exists leaks the order book.
    get("/order/getById/" + orderId).andExpect(status().isNotFound());
    delete("/order/deleteById/" + orderId).andExpect(status().isNotFound());
  }

  @Test
  void an_account_with_no_client_should_not_be_able_to_hold_a_cart() throws Exception {
    authenticateAs(Role.ADMIN);

    get("/order/getCart").andExpect(status().isBadRequest());
    post("/order/addProduct", addRequest(colaId, 1f)).andExpect(status().isBadRequest());
  }

  /**
   * 403 rather than 401 because SecurityConfig registers no authenticationEntryPoint, so Spring
   * Security falls back to its default. Pre-existing behaviour across every authenticated endpoint,
   * asserted here as-is rather than changed under an order ticket.
   */
  @Test
  void an_anonymous_caller_should_be_rejected() throws Exception {
    authenticateAsAnonymous();

    get("/order/getCart").andExpect(status().isForbidden());
  }

  @Test
  void a_missing_price_type_should_be_a_bad_request_not_a_server_error() throws Exception {
    post("/order/addProduct", Map.of("storageId", storageId, "productId", colaId, "quantity", 1f))
        .andExpect(status().isBadRequest());
  }

  private AddProductToOrderRequest addRequest(Integer productId, float quantity) {
    return new AddProductToOrderRequest(storageId, productId, PriceType.LOCAL, quantity);
  }

  private Integer saveProduct(String name, String erpCode, float weight, float price) {
    ProductEntity product = new ProductEntity(
        null, name, "buc", 1f, erpCode, null, weight, null, null,
        Instant.now(), null, null,
        Set.of(new PriceEntity(PriceType.LOCAL, AggregateReference.to(storageId), price)));
    return productRepository.save(product).getId();
  }
}
