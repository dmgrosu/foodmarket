package md.ramaiana.foodmarket.domain.brand.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.brand.core.response.BrandResponse;
import md.ramaiana.foodmarket.domain.brand.core.usecase.AdminBrandSearchUseCase;
import md.ramaiana.foodmarket.domain.brand.core.usecase.BrandSearchCriteria;
import md.ramaiana.foodmarket.domain.brand.presentation.voter.AdminBrandAccessVoter;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Brand controller.
 */
@Validated
@RestController
@RequestMapping("/admin/brand")
@RequiredArgsConstructor
public class AdminBrandController {

  // Access voters
  private final AdminBrandAccessVoter accessVoter;

  // Use cases
  private final AdminBrandSearchUseCase adminBrandSearchUseCase;

  /**
   * Search brands.
   */
  @GetMapping("/search")
  public PagedResponse<BrandResponse> search(
      @RequestParam(value = "name", required = false) @Nullable String name,
      @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "25") @Min(1) int pageSize,
      @RequestParam(value = "sortColumn", defaultValue = "name") String sortColumn,
      @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection) {
    accessVoter.assertCanSearch();
    return adminBrandSearchUseCase.execute(new BrandSearchCriteria(name, pageNo, pageSize, sortColumn, sortDirection));
  }
}
