package md.ramaiana.foodmarket.domain.product.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import md.ramaiana.foodmarket.BaseControllerTest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import org.junit.jupiter.api.Test;

class ProductControllerTest extends BaseControllerTest {

  @Test
  void whenListGroups_thenReturnsGroups() throws Exception {
    AppUserEntity user = testAppUserService.create();
    testProductGroupService.create("Fruits", null);
    testProductGroupService.create("Vegetables", null);

    doGet("/product/listGroups", user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groups.length()").value(2))
        .andExpect(jsonPath("$.products").isEmpty());
  }

  @Test
  void whenListProducts_thenReturnsProducts() throws Exception {
    AppUserEntity user = testAppUserService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    testProductService.create(brand.getId(), group.getId());
    testProductService.create(brand.getId(), group.getId());

    doGet("/product/listProducts?groupId=" + group.getId(), user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.products.length()").value(2));
  }

  @Test
  void whenSearch_thenReturnsFiltered() throws Exception {
    AppUserEntity user = testAppUserService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    testProductService.create("Apple", 5f, 1f, brand.getId(), group.getId());
    testProductService.create("Banana", 3f, 1f, brand.getId(), group.getId());

    doRequest(get("/product/search").param("name", "%apple%"), user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.products.length()").value(1));
  }

  @Test
  void whenUnauthenticated_thenReturns403() throws Exception {
    doGet("/product/listGroups")
        .andExpect(status().isForbidden());
  }
}
