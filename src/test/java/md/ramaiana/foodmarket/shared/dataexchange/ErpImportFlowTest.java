package md.ramaiana.foodmarket.shared.dataexchange;

import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductGroupSearchUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportBalancesUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportClientsUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportProductsUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Drives the three ERP importers end to end against fixtures cut from the real export, over the real
 * repositories rather than mocks. The fixtures keep the shapes the live files actually have and that
 * the importers used to choke on:
 * <ul>
 *   <li>every {@code <group>} mirrors a product, and the code products are filed under is never
 *       declared as a group of its own;</li>
 *   <li>clients carrying a fiscal code that is blank or wider than {@code client.idno};</li>
 *   <li>the same fiscal code appearing on more than one client.</li>
 * </ul>
 */
@Tag("integration")
@SpringBootTest
// The app schedules the same importers against the same beans; a long delay leaves only the fire at
// context start-up, which happens before this test points those beans at its own folder.
@TestPropertySource(properties = "dataLoadingDelay=3600000")
class ErpImportFlowTest {

    private static final Path FIXTURES = Paths.get("src/test/resources/importFixtures");
    private static final List<String> DATA_FILES =
            List.of("products-data.xml", "clients-data.xml", "balances-data.xml");
    /** Product and mirror-group code of the first product in the fixture. */
    private static final String PRODUCT_ERP_CODE = "00003715";
    /** The code that product is filed under. The fixture never declares it as a group. */
    private static final String UNDECLARED_GROUP_ERP_CODE = "001242";

    @Autowired
    ImportProductsUseCase importProducts;
    @Autowired
    ImportClientsUseCase importClients;
    @Autowired
    ImportBalancesUseCase importBalances;
    @Autowired
    ProductGroupSearchUseCase groupSearch;
    @Autowired
    JdbcTemplate jdbc;

    Path exchangeFolder;

    @BeforeEach
    void setUp() throws IOException {
        exchangeFolder = Paths.get("target/erp-import-flow-test");
        Files.createDirectories(exchangeFolder);
        for (String name : DATA_FILES) {
            Files.copy(FIXTURES.resolve(name), exchangeFolder.resolve(name),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        // Deliberately without a trailing separator - the importers must not depend on one.
        String folder = exchangeFolder.toAbsolutePath().toString();
        importProducts.setExchangeFolderPath(folder);
        importClients.setExchangeFolderPath(folder);
        importBalances.setExchangeFolderPath(folder);

        wipeImportedData();
        for (String erpCode : List.of("1", "2", "3", "5", "6", "8")) {
            jdbc.update("INSERT INTO storages (name, erp_code) VALUES (?, ?)", "storage " + erpCode, erpCode);
        }
    }

    @AfterEach
    void tearDown() {
        // The in-memory database is shared with every other @SpringBootTest in the run.
        wipeImportedData();
    }

    /**
     * Clearing each table outright is the intent - these are the tables the importers own, and the
     * in-memory database is shared with every other {@code @SpringBootTest} in the run. Child tables
     * come first so the foreign keys stay satisfied at every step.
     */
    private void wipeImportedData() {
        for (String table : List.of("prices", "balances", "product", "product_group", "brand",
                "client_addresses", "client_phones", "client", "storages")) {
            //noinspection SqlWithoutWhere - clearing the table is exactly what this is for
            jdbc.execute("DELETE FROM " + table);
        }
    }

    @Test
    void imports_the_catalogue_and_consumes_the_file() {
        importProducts.execute();

        assertThat(count("product")).isEqualTo(12);
        assertThat(count("prices")).isPositive();
        assertThat(exchangeFolder.resolve("products-data.xml")).doesNotExist();
    }

    @Test
    void stores_the_brand_name_and_erp_code_the_right_way_round() {
        importProducts.execute();

        assertThat(jdbc.queryForList("SELECT name, erp_code FROM brand ORDER BY erp_code"))
                .extracting(row -> row.get("NAME"), row -> row.get("ERP_CODE"))
                .contains(tuple("empty", "BUH-013"));
    }

    @Test
    void creates_the_group_products_are_filed_under_even_though_the_file_never_declares_it() {
        importProducts.execute();

        Map<String, Object> group = jdbc.queryForMap(
                "SELECT id, name, parent_group_id FROM product_group WHERE erp_code = ?",
                UNDECLARED_GROUP_ERP_CODE);
        // No name to take from the file, so the code stands in until an export declares one.
        assertThat(group.get("NAME")).isEqualTo(UNDECLARED_GROUP_ERP_CODE);
        assertThat(group.get("PARENT_GROUP_ID")).isNull();

        // The file names no group under this code, so the name is derived from the products filed
        // under it: the words all of their names start with. Asserted as a relationship rather than
        // a literal, because the live names are Cyrillic.
        String derivedName = (String) group.get("NAME");
        assertThat(derivedName).isNotEqualTo(UNDECLARED_GROUP_ERP_CODE);
        List<String> productNames = jdbc.queryForList(
                "SELECT name FROM product WHERE group_id = ?", String.class, group.get("ID"));
        assertThat(productNames).isNotEmpty().allMatch(name -> name.startsWith(derivedName));

        Map<String, Object> product = jdbc.queryForMap(
                "SELECT group_id, brand_id FROM product WHERE erp_code = ?", PRODUCT_ERP_CODE);
        assertThat(product.get("GROUP_ID")).isEqualTo(group.get("ID"));
        assertThat(product.get("BRAND_ID")).isNotNull();
    }

    @Test
    void files_the_groups_the_erp_left_loose_under_a_derived_folder() {
        importProducts.execute();

        Map<String, Object> folder = jdbc.queryForMap(
                "SELECT pg.id, pg.name, pg.erp_code FROM product_group pg "
                        + "JOIN product_group child ON child.parent_group_id = pg.id "
                        + "WHERE child.erp_code = ?", UNDECLARED_GROUP_ERP_CODE);

        // The folder is named for the word its groups' names share, and carries an ERP code that
        // cannot collide with a real one.
        assertThat((String) folder.get("ERP_CODE")).startsWith("derived:");
        String folderName = (String) folder.get("NAME");
        assertThat(folderName).isEqualTo(((String) folder.get("ERP_CODE")).substring("derived:".length()));

        List<String> filedUnderIt = jdbc.queryForList(
                "SELECT name FROM product_group WHERE parent_group_id = ?", String.class, folder.get("ID"));
        assertThat(filedUnderIt).hasSizeGreaterThan(1).allMatch(name -> name.startsWith(folderName));
    }

    @Test
    void keeps_a_declared_group_whose_parent_is_undeclared_and_hangs_it_off_that_parent() {
        importProducts.execute();

        Map<String, Object> mirrorGroup = jdbc.queryForMap(
                "SELECT name, parent_group_id FROM product_group WHERE erp_code = ?", PRODUCT_ERP_CODE);
        Integer parentId = jdbc.queryForObject(
                "SELECT id FROM product_group WHERE erp_code = ?", Integer.class, UNDECLARED_GROUP_ERP_CODE);
        // The export gives the mirror group the name of the product it shadows, so the two must match
        // rather than be spelled out here - the file is Windows-1251 and the name is Cyrillic.
        String productName = jdbc.queryForObject(
                "SELECT name FROM product WHERE erp_code = ?", String.class, PRODUCT_ERP_CODE);
        assertThat(mirrorGroup.get("NAME")).isEqualTo(productName);
        assertThat(mirrorGroup.get("PARENT_GROUP_ID")).isEqualTo(parentId);
    }

    @Test
    void re_importing_the_same_catalogue_updates_rows_instead_of_duplicating_them() throws IOException {
        importProducts.execute();
        long groupsAfterFirstRun = count("product_group");
        Files.copy(FIXTURES.resolve("products-data.xml"),
                exchangeFolder.resolve("products-data.xml"), StandardCopyOption.REPLACE_EXISTING);

        importProducts.execute();

        assertThat(count("product")).isEqualTo(12);
        assertThat(count("product_group")).isEqualTo(groupsAfterFirstRun);
        assertThat(count("brand")).isEqualTo(3);
        assertThat(count("prices")).isPositive();
    }

    @Test
    void imports_balances_for_products_the_catalogue_knows() {
        importProducts.execute();

        importBalances.execute();

        assertThat(count("balances")).isPositive();
        assertThat(exchangeFolder.resolve("balances-data.xml")).doesNotExist();
    }

    @Test
    void imports_the_clients_a_malformed_row_used_to_take_down_with_it() {
        importClients.execute();

        // 11 clients in the fixture: 2 with a fiscal code wider than the column, 2 with a blank one.
        assertThat(count("client")).isEqualTo(7);
        assertThat(count("client_addresses")).isPositive();
        assertThat(exchangeFolder.resolve("clients-data.xml")).doesNotExist();
    }

    @Test
    void surfaces_imported_groups_through_the_catalogue_search() {
        importProducts.execute();
        importBalances.execute();
        Integer storageId = jdbc.queryForObject(
                "SELECT id FROM storages WHERE erp_code = '1'", Integer.class);

        List<ProductGroupResponse> roots = groupSearch.execute(storageId, null);

        assertThat(roots).isNotEmpty();
    }

    private long count(String table) {
        Long rows = jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return rows == null ? 0 : rows;
    }
}
