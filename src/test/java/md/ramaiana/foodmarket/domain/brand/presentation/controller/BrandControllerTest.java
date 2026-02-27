package md.ramaiana.foodmarket.domain.brand.presentation.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import md.ramaiana.foodmarket.BaseControllerTest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import org.junit.jupiter.api.Test;

class BrandControllerTest extends BaseControllerTest {

  @Test
  void whenAuthenticated_thenReturnsBrands() throws Exception {
    AppUserEntity user = testAppUserService.create();
    testBrandService.create("Brand A", "ERP-A");
    testBrandService.create("Brand B", "ERP-B");

    doGet("/brand/getAll", user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[1].name").exists());
  }

  @Test
  void whenUnauthenticated_thenReturns403() throws Exception {
    doGet("/brand/getAll")
        .andExpect(status().isForbidden());
  }
}
