package md.ramaiana.foodmarket.domain.product.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductGroupSearchUseCaseTest extends BaseTest {

  @Autowired
  private ProductGroupSearchUseCase productGroupSearchUseCase;

  @Test
  void whenRootGroups_thenReturnsSortedByName() {
    testProductGroupService.create("Zebra Group", null);
    testProductGroupService.create("Alpha Group", null);
    testProductGroupService.create("Mango Group", null);

    ProductListResponse response = productGroupSearchUseCase.execute(null);

    assertThat(response.getGroups()).hasSize(3);
    assertThat(response.getGroups()).extracting(ProductGroupResponse::getName)
        .containsExactly("Alpha Group", "Mango Group", "Zebra Group");
    assertThat(response.getProducts()).isEmpty();
  }

  @Test
  void whenParentGroupId_thenReturnsChildren() {
    ProductGroupEntity parent = testProductGroupService.create("Parent", null);
    testProductGroupService.create("Child A", parent.getId());
    testProductGroupService.create("Child B", parent.getId());

    ProductListResponse response = productGroupSearchUseCase.execute(parent.getId());

    assertThat(response.getGroups()).hasSize(2);
    assertThat(response.getGroups()).extracting(ProductGroupResponse::getName)
        .containsExactly("Child A", "Child B");
  }

  @Test
  void whenGroupsHaveChildren_thenLoadsRecursiveHierarchy() {
    ProductGroupEntity root = testProductGroupService.create("Root", null);
    ProductGroupEntity mid = testProductGroupService.create("Mid", root.getId());
    testProductGroupService.create("Leaf", mid.getId());

    ProductListResponse response = productGroupSearchUseCase.execute(null);

    assertThat(response.getGroups()).hasSize(1);
    ProductGroupResponse rootResponse = response.getGroups().getFirst();
    assertThat(rootResponse.getName()).isEqualTo("Root");
    assertThat(rootResponse.getChildren()).hasSize(1);
    assertThat(rootResponse.getChildren().getFirst().getName()).isEqualTo("Mid");
    assertThat(rootResponse.getChildren().getFirst().getChildren()).hasSize(1);
    assertThat(rootResponse.getChildren().getFirst().getChildren().getFirst().getName()).isEqualTo("Leaf");
  }
}
