package md.ramaiana.foodmarket.domain.product.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductSearchUseCaseTest extends BaseTest {

  @Autowired
  private ProductSearchUseCase productSearchUseCase;

  @Test
  void whenFilterByGroup_thenReturnsMatchingProducts() {
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group1 = testProductGroupService.create();
    ProductGroupEntity group2 = testProductGroupService.create();
    testProductService.create(brand.getId(), group1.getId());
    testProductService.create(brand.getId(), group2.getId());

    ProductListResponse response = productSearchUseCase.execute(group1.getId(), null, null);

    assertThat(response.getProducts()).hasSize(1);
    assertThat(response.getProducts().getFirst().getGroupId()).isEqualTo(group1.getId());
  }

  @Test
  void whenFilterByBrand_thenReturnsMatchingProducts() {
    BrandEntity brand1 = testBrandService.create("Brand A", "ERP-A");
    BrandEntity brand2 = testBrandService.create("Brand B", "ERP-B");
    ProductGroupEntity group = testProductGroupService.create();
    testProductService.create(brand1.getId(), group.getId());
    testProductService.create(brand2.getId(), group.getId());

    ProductListResponse response = productSearchUseCase.execute(null, brand1.getId(), null);

    assertThat(response.getProducts()).hasSize(1);
    assertThat(response.getProducts().getFirst().getBrandId()).isEqualTo(brand1.getId());
  }

  @Test
  void whenFilterByName_thenReturnsMatchingProducts() {
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    testProductService.create("Apple Juice", 5f, 1f, brand.getId(), group.getId());
    testProductService.create("Orange Juice", 6f, 1f, brand.getId(), group.getId());
    testProductService.create("Milk", 3f, 1f, brand.getId(), group.getId());

    ProductListResponse response = productSearchUseCase.execute(null, null, "%juice%");

    assertThat(response.getProducts()).hasSize(2);
    assertThat(response.getProducts()).extracting(ProductResponse::getName)
        .allMatch(name -> name.toLowerCase().contains("juice"));
  }

  @Test
  void whenNoFilters_thenReturnsAllProducts() {
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    testProductService.create(brand.getId(), group.getId());
    testProductService.create(brand.getId(), group.getId());
    testProductService.create(brand.getId(), group.getId());

    ProductListResponse response = productSearchUseCase.execute(null, null, null);

    assertThat(response.getProducts()).hasSize(3);
  }

  @Test
  void whenProductsExist_thenBuildsGroupHierarchy() {
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity parentGroup = testProductGroupService.create("Beverages", null);
    ProductGroupEntity childGroup = testProductGroupService.create("Juices", parentGroup.getId());
    testProductService.create(brand.getId(), childGroup.getId());

    ProductListResponse response = productSearchUseCase.execute(null, null, null);

    assertThat(response.getGroups()).hasSize(1);
    assertThat(response.getGroups().getFirst().getName()).isEqualTo("Beverages");
    assertThat(response.getGroups().getFirst().getChildren()).hasSize(1);
    assertThat(response.getGroups().getFirst().getChildren().getFirst().getName()).isEqualTo("Juices");
  }
}
