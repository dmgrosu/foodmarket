package md.ramaiana.foodmarket.domain.product.core.usecase;

import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Paging over the catalogue, against the real repositories.
 * <p>
 * The fixture is written straight through JDBC rather than imported, so each case can state the
 * exact stock situation it is about — a product in two storages, a product with none.
 */
@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = "dataLoadingDelay=3600000")
class ProductSearchUseCaseTest {

    private static final int PAGE_SIZE = 4;
    private static final int IN_STOCK_PRODUCTS = 10;

    @Autowired
    ProductSearchUseCase useCase;
    @Autowired
    ProductGroupSearchUseCase groupSearchUseCase;
    @Autowired
    JdbcTemplate jdbc;

    int groupId;
    int otherGroupId;
    int brandId;
    int firstStorageId;
    int secondStorageId;

    @BeforeEach
    void setUp() {
        wipe();
        jdbc.update("INSERT INTO brand (name, erp_code, created_at) VALUES ('Bucuria', 'B-1', CURRENT_TIMESTAMP)");
        brandId = id("SELECT id FROM brand WHERE erp_code = 'B-1'");

        jdbc.update("INSERT INTO product_group (name, erp_code, created_at) VALUES ('Sweets', 'G-1', CURRENT_TIMESTAMP)");
        groupId = id("SELECT id FROM product_group WHERE erp_code = 'G-1'");
        jdbc.update("INSERT INTO product_group (name, parent_group_id, erp_code, created_at) VALUES ('Chocolate', ?, 'G-2', CURRENT_TIMESTAMP)",
                groupId);
        otherGroupId = id("SELECT id FROM product_group WHERE erp_code = 'G-2'");

        jdbc.update("INSERT INTO storages (name, erp_code) VALUES ('First', 'S-1')");
        jdbc.update("INSERT INTO storages (name, erp_code) VALUES ('Second', 'S-2')");
        firstStorageId = id("SELECT id FROM storages WHERE erp_code = 'S-1'");
        secondStorageId = id("SELECT id FROM storages WHERE erp_code = 'S-2'");

        // Ten in-stock products, named so that ascending order is predictable.
        for (int i = 0; i < IN_STOCK_PRODUCTS; i++) {
            int productId = insertProduct(String.format("Product %02d", i), groupId);
            jdbc.update("INSERT INTO balances (storage_id, product_id, quantity) VALUES (?, ?, 5)",
                    firstStorageId, productId);
        }
        // Stocked in both storages: it must still occupy exactly one row of one page.
        int inBothStorages = insertProduct("Product in both", groupId);
        jdbc.update("INSERT INTO balances (storage_id, product_id, quantity) VALUES (?, ?, 3)",
                firstStorageId, inBothStorages);
        jdbc.update("INSERT INTO balances (storage_id, product_id, quantity) VALUES (?, ?, 7)",
                secondStorageId, inBothStorages);

        // No balance row at all, and a zero-quantity one: neither is in stock.
        insertProduct("Product never stocked", groupId);
        int outOfStock = insertProduct("Product out of stock", groupId);
        jdbc.update("INSERT INTO balances (storage_id, product_id, quantity) VALUES (?, ?, 0)",
                firstStorageId, outOfStock);
    }

    @AfterEach
    void tearDown() {
        wipe();
    }

    @Test
    void pages_through_the_group_without_repeating_or_dropping_a_product() {
        PagedResponse<ProductResponse> firstPage = search(0, PAGE_SIZE, "name");

        // 10 singly-stocked products plus the one held in two storages.
        assertThat(firstPage.totalElements()).isEqualTo(IN_STOCK_PRODUCTS + 1);
        assertThat(firstPage.totalPages()).isEqualTo(3);
        assertThat(firstPage.currentPage()).isZero();
        assertThat(firstPage.items()).hasSize(PAGE_SIZE);

        List<Integer> seen = new ArrayList<>();
        for (int pageNo = 0; pageNo < firstPage.totalPages(); pageNo++) {
            seen.addAll(search(pageNo, PAGE_SIZE, "name").items().stream().map(ProductResponse::id).toList());
        }
        assertThat(seen).hasSize(IN_STOCK_PRODUCTS + 1).doesNotHaveDuplicates();
    }

    @Test
    void counts_a_product_stocked_in_two_storages_once() {
        List<String> names = search(0, 50, "name").items().stream().map(ProductResponse::name).toList();

        assertThat(names).filteredOn("Product in both"::equals).hasSize(1);
    }

    @Test
    void leaves_out_products_with_no_positive_balance() {
        List<String> names = search(0, 50, "name").items().stream().map(ProductResponse::name).toList();

        assertThat(names).doesNotContain("Product never stocked", "Product out of stock");
    }

    @Test
    void sorts_descending_when_asked() {
        PagedResponse<ProductResponse> page = useCase.execute(new ProductSearchCriteria(
                null, groupId, null, null, 0, PAGE_SIZE, "name", Sort.Direction.DESC));

        assertThat(page.items()).first()
                .extracting(ProductResponse::name)
                .isEqualTo("Product in both");
    }

    @Test
    void rejects_a_sort_column_outside_the_whitelist() {
        assertThatThrownBy(() -> search(0, PAGE_SIZE, "erpCode"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("erpCode");
    }

    @Test
    void carries_each_product_s_prices_through_the_paged_load() {
        int productId = search(0, 1, "name").items().getFirst().id();
        jdbc.update("INSERT INTO prices (\"type\", product_id, storage_id, price) VALUES ('LOCAL', ?, ?, 12.5)",
                productId, firstStorageId);

        ProductResponse product = search(0, 1, "name").items().getFirst();

        assertThat(product.prices()).extracting("type", "price").containsExactly(tuple("LOCAL", 12.5f));
    }

    @Test
    void searches_across_groups_and_says_which_group_each_hit_came_from() {
        int chocolateProduct = insertProduct("Truffle", otherGroupId);
        jdbc.update("INSERT INTO balances (storage_id, product_id, quantity) VALUES (?, ?, 2)",
                firstStorageId, chocolateProduct);

        // No groupId: the search reaches products the selected group does not contain.
        PagedResponse<ProductResponse> page = useCase.execute(new ProductSearchCriteria(
                null, null, null, "Truffle", 0, PAGE_SIZE, "name", Sort.Direction.ASC));

        assertThat(page.items()).extracting(ProductResponse::name, ProductResponse::groupName)
                .containsExactly(tuple("Truffle", "Chocolate"));
    }

    @Test
    void still_lists_a_single_group_when_one_is_given() {
        PagedResponse<ProductResponse> page = search(0, 50, "name");

        assertThat(page.items()).extracting(ProductResponse::groupName).containsOnly("Sweets");
    }

    private PagedResponse<ProductResponse> search(int pageNo, int pageSize, String sortColumn) {
        return useCase.execute(new ProductSearchCriteria(null, groupId, null, null,
                pageNo, pageSize, sortColumn, Sort.Direction.ASC));
    }

    private int insertProduct(String name, int inGroupId) {
        jdbc.update("INSERT INTO product (name, brand_id, group_id, package, erp_code, created_at) "
                        + "VALUES (?, ?, ?, 1, ?, CURRENT_TIMESTAMP)",
                name, brandId, inGroupId, "P-" + name);
        return id("SELECT id FROM product WHERE erp_code = 'P-" + name + "'");
    }

    private int id(String sql) {
        Integer value = jdbc.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private void wipe() {
        for (String table : List.of("prices", "balances", "product", "product_group", "brand", "storages")) {
            //noinspection SqlWithoutWhere - clearing the table is exactly what this is for
            jdbc.execute("DELETE FROM " + table);
        }
    }
}
