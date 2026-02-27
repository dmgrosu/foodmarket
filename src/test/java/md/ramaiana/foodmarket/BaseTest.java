package md.ramaiana.foodmarket;

import md.ramaiana.foodmarket.testservices.TestAppUserService;
import md.ramaiana.foodmarket.testservices.TestBrandService;
import md.ramaiana.foodmarket.testservices.TestClientService;
import md.ramaiana.foodmarket.testservices.TestOrderService;
import md.ramaiana.foodmarket.testservices.TestProductGroupService;
import md.ramaiana.foodmarket.testservices.TestProductService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {

  @Autowired
  protected TestClientService testClientService;

  @Autowired
  protected TestBrandService testBrandService;

  @Autowired
  protected TestProductGroupService testProductGroupService;

  @Autowired
  protected TestProductService testProductService;

  @Autowired
  protected TestAppUserService testAppUserService;

  @Autowired
  protected TestOrderService testOrderService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  void cleanupDatabase() {
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
    jdbcTemplate.execute("TRUNCATE TABLE order_product");
    jdbcTemplate.execute("TRUNCATE TABLE \"order\"");
    jdbcTemplate.execute("TRUNCATE TABLE product");
    jdbcTemplate.execute("TRUNCATE TABLE product_group");
    jdbcTemplate.execute("TRUNCATE TABLE brand");
    jdbcTemplate.execute("TRUNCATE TABLE app_user_roles");
    jdbcTemplate.execute("TRUNCATE TABLE app_user");
    jdbcTemplate.execute("TRUNCATE TABLE client");
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
  }
}
