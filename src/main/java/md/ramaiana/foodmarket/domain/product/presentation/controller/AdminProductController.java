package md.ramaiana.foodmarket.domain.product.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.core.usecase.AdminProductSearchCriteria;
import md.ramaiana.foodmarket.domain.product.core.usecase.AdminProductSearchUseCase;
import md.ramaiana.foodmarket.domain.product.presentation.voter.AdminProductAccessVoter;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Product controller.
 */
@Validated
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

  // Access voters
  private final AdminProductAccessVoter accessVoter;

  // Use cases
  private final AdminProductSearchUseCase adminProductSearchUseCase;

  /**
   * Search products.
   */
  @GetMapping("/search")
  public PagedResponse<ProductResponse> search(
      @RequestParam(value = "name", required = false) @Nullable String name,
      @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
      @RequestParam(value = "groupId", required = false) @Nullable Integer groupId,
      @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "25") @Min(1) int pageSize,
      @RequestParam(value = "sortColumn", defaultValue = "name") String sortColumn,
      @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection) {
    accessVoter.assertCanSearch();
    return adminProductSearchUseCase.execute(
        new AdminProductSearchCriteria(name, brandId, groupId, pageNo, pageSize, sortColumn, sortDirection));
  }
}
