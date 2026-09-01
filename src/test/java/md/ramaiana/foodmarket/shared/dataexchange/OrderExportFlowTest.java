package md.ramaiana.foodmarket.shared.dataexchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.transform.stream.StreamSource;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ExportOrdersUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpOrderDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpOrderItemDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.OrdersDataDto;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Drives the outbound half of the ERP exchange over real repositories: placed orders out to an
 * {@code orders-data-*.xml} file, and the orders marked handed over afterwards.
 * <p>
 * The document produced here is fed straight back through the application's own {@link Unmarshaller},
 * which is what proves it is well-formed and that every attribute the ERP reads actually binds.
 */
@Tag("integration")
@SpringBootTest
// The app schedules the same exporter against the same bean; a long delay leaves only the fire at
// context start-up, which happens before this test points the bean at its own folder.
@TestPropertySource(properties = "dataLoadingDelay=3600000")
class OrderExportFlowTest {

  private static final String CLIENT_ERP_CODE = "R01-636";
  private static final String CLIENT_IDNO = "1020600029080";
  private static final String STORAGE_ERP_CODE = "ST-EXPORT";
  private static final String COLA_ERP_CODE = "00003715";
  private static final String BREAD_ERP_CODE = "00003716";

  @Autowired
  private ExportOrdersUseCase exportOrders;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private ClientRepository clientRepository;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private StorageRepository storageRepository;
  @Autowired
  private Unmarshaller unmarshaller;
  @Autowired
  private JdbcTemplate jdbc;

  private Path exchangeFolder;
  private Integer clientId;
  private Integer storageId;
  private Integer colaId;
  private Integer breadId;

  @BeforeEach
  void setUp() throws IOException {
    exchangeFolder = Paths.get("target/order-export-flow-test");
    if (Files.exists(exchangeFolder)) {
      deleteFiles(exchangeFolder);
    }
    // Deliberately without a trailing separator - the exporter must not depend on one.
    exportOrders.setExchangeFolderPath(exchangeFolder.toAbsolutePath().toString());

    clientId = clientRepository.save(new ClientEntity(
        "Export Client", CLIENT_IDNO, null, CLIENT_ERP_CODE, Set.of(), Set.of())).getId();
    storageId = storageRepository.save(new StorageEntity(null, "Main", STORAGE_ERP_CODE)).getId();
    colaId = saveProduct("Cola", COLA_ERP_CODE, 1.5f);
    breadId = saveProduct("Bread", BREAD_ERP_CODE, 0.5f);
  }

  @AfterEach
  void cleanUp() throws IOException {
    jdbc.update("DELETE FROM order_product");
    jdbc.update("DELETE FROM \"order\"");
    jdbc.update("DELETE FROM prices");
    jdbc.update("DELETE FROM product");
    jdbc.update("DELETE FROM storages");
    jdbc.update("DELETE FROM client");
    deleteFiles(exchangeFolder);
  }

  @Test
  void should_write_every_placed_order_into_one_file() throws Exception {
    int firstId = placeOrder(colaId, 2f, 10f);
    int secondId = placeOrder(breadId, 3f, 4f);

    exportOrders.execute();

    OrdersDataDto exported = readSingleExportFile();
    assertThat(exported.getOrders())
        .extracting(ErpOrderDto::getId, ErpOrderDto::getClientCode, ErpOrderDto::getClientIdno,
            ErpOrderDto::getStorageCode, ErpOrderDto::getPriceType)
        .containsExactly(
            tuple(firstId, CLIENT_ERP_CODE, CLIENT_IDNO, STORAGE_ERP_CODE, "LOCAL"),
            tuple(secondId, CLIENT_ERP_CODE, CLIENT_IDNO, STORAGE_ERP_CODE, "LOCAL"));
  }

  @Test
  void should_name_products_by_their_erp_code_and_carry_the_line_figures() throws Exception {
    placeOrder(colaId, 2f, 10f);

    exportOrders.execute();

    ErpOrderDto order = readSingleExportFile().getOrders().getFirst();
    assertThat(order.getTotalSum()).isEqualTo(20f);
    assertThat(order.getTotalWeight()).isEqualTo(3f);
    assertThat(order.getPlacedAt()).isNotBlank();
    assertThat(order.getItems())
        .extracting(ErpOrderItemDto::getProductCode, ErpOrderItemDto::getQuantity,
            ErpOrderItemDto::getPrice, ErpOrderItemDto::getSum, ErpOrderItemDto::getWeight)
        .containsExactly(tuple(COLA_ERP_CODE, 2f, 10f, 20f, 3f));
  }

  @Test
  void should_mark_exported_orders_so_the_next_cycle_leaves_them_alone() throws Exception {
    int orderId = placeOrder(colaId, 1f, 10f);

    exportOrders.execute();

    OrderEntity exported = orderRepository.findById(orderId).orElseThrow();
    assertThat(exported.getState().name()).isEqualTo("EXPORTED");
    assertThat(exported.getExportedAt()).isNotNull();

    exportOrders.execute();

    assertThat(exportFiles()).hasSize(1);
  }

  @Test
  void should_write_nothing_when_no_order_is_waiting() throws Exception {
    exportOrders.execute();

    assertThat(exportFiles()).isEmpty();
  }

  @Test
  void should_leave_no_partial_file_behind() throws Exception {
    placeOrder(colaId, 1f, 10f);

    exportOrders.execute();

    // The ERP globs orders-data-*.xml; a .tmp left in the folder would mean a reader could see a
    // half-written document.
    try (Stream<Path> files = Files.list(exchangeFolder)) {
      assertThat(files.map(path -> path.getFileName().toString()))
          .allSatisfy(name -> assertThat(name).endsWith(".xml"));
    }
  }

  /**
   * Pins the wire format. Every name here is one the ERP reads, so renaming a field on
   * {@code ErpOrderDto} is not a refactor — it silently breaks the other side of the exchange, which
   * has no compiler to catch it. Change this test only alongside the ERP.
   */
  @Test
  void should_keep_the_element_and_attribute_names_the_erp_reads() throws Exception {
    placeOrder(colaId, 2f, 10f);

    exportOrders.execute();

    String xml = Files.readString(exportFiles().getFirst());
    assertThat(xml).contains("<orders-data>", "<orders>", "<order ", "<item ");
    assertThat(attributeNamesOf(xml, "order")).containsExactlyInAnyOrder(
        "id", "createdAt", "placedAt", "clientCode", "clientIdno",
        "storageCode", "priceType", "totalSum", "totalWeight");
    assertThat(attributeNamesOf(xml, "item")).containsExactlyInAnyOrder(
        "productCode", "quantity", "price", "sum", "weight");
  }

  @Test
  void should_not_export_a_cart_that_was_never_placed() throws Exception {
    OrderEntity cart = new OrderEntity(clientId, storageId, PriceType.LOCAL);
    cart.addProduct(colaId, 10f, 1.5f, 1f);
    orderRepository.save(cart);

    exportOrders.execute();

    assertThat(exportFiles()).isEmpty();
  }

  @Test
  void should_not_export_a_deleted_order() throws Exception {
    int orderId = placeOrder(colaId, 1f, 10f);
    OrderEntity placed = orderRepository.findById(orderId).orElseThrow();
    placed.markDeleted();
    orderRepository.save(placed);

    exportOrders.execute();

    assertThat(exportFiles()).isEmpty();
  }

  private int placeOrder(Integer productId, float quantity, float price) {
    OrderEntity order = new OrderEntity(clientId, storageId, PriceType.LOCAL);
    order.addProduct(productId, price, productId.equals(colaId) ? 1.5f : 0.5f, quantity);
    order.place();
    return orderRepository.save(order).getId();
  }

  private Integer saveProduct(String name, String erpCode, float weight) {
    return productRepository.save(new ProductEntity(
        null, name, "buc", 1f, erpCode, null, weight, null, null,
        Instant.now(), null, null,
        Set.of(new PriceEntity(PriceType.LOCAL, AggregateReference.to(storageId), 10f)))).getId();
  }

  private OrdersDataDto readSingleExportFile() throws Exception {
    List<Path> files = exportFiles();
    assertThat(files).hasSize(1);
    assertThat(files.getFirst().getFileName().toString())
        .startsWith("orders-data-")
        .endsWith(".xml");

    try (FileInputStream in = new FileInputStream(files.getFirst().toFile())) {
      return (OrdersDataDto) unmarshaller.unmarshal(new StreamSource(in));
    }
  }

  /**
   * The attribute names carried by the first {@code <tag ...>} in the document.
   */
  private List<String> attributeNamesOf(String xml, String tag) {
    Matcher element = Pattern.compile("<" + tag + " ([^>]*?)/?>").matcher(xml);
    assertThat(element.find()).as("no <%s> element in the document", tag).isTrue();

    List<String> names = new ArrayList<>();
    Matcher attribute = Pattern.compile("(\\w+)=\"").matcher(element.group(1));
    while (attribute.find()) {
      names.add(attribute.group(1));
    }
    return names;
  }

  private List<Path> exportFiles() throws IOException {
    if (!Files.exists(exchangeFolder)) {
      return List.of();
    }
    try (Stream<Path> files = Files.list(exchangeFolder)) {
      return files.filter(path -> path.getFileName().toString().endsWith(".xml"))
          .sorted(Comparator.comparing(Path::getFileName))
          .toList();
    }
  }

  private void deleteFiles(Path folder) throws IOException {
    if (!Files.exists(folder)) {
      return;
    }
    try (Stream<Path> files = Files.list(folder)) {
      for (Path file : files.toList()) {
        Files.deleteIfExists(file);
      }
    }
  }
}
