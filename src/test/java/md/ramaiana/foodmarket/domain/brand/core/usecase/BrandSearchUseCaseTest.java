package md.ramaiana.foodmarket.domain.brand.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.core.response.BrandResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BrandSearchUseCaseTest extends BaseTest {

  @Autowired
  private BrandSearchUseCase brandSearchUseCase;

  @Test
  void whenBrandsExist_thenReturnsAll() {
    testBrandService.create("Alpha", "ERP-A");
    testBrandService.create("Beta", "ERP-B");

    List<BrandResponse> result = brandSearchUseCase.execute();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(BrandResponse::getName)
        .containsExactlyInAnyOrder("Alpha", "Beta");
  }

  @Test
  void whenNoBrands_thenReturnsEmptyList() {
    List<BrandResponse> result = brandSearchUseCase.execute();

    assertThat(result).isEmpty();
  }
}
