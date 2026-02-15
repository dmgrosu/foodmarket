package md.ramaiana.foodmarket.domain.product.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductGroupSearchUseCase;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductSearchUseCase;
import md.ramaiana.foodmarket.domain.product.presentation.voter.ProductAccessVoter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product controller.
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management endpoints")
public class ProductController {

  // Access voters
  private final ProductAccessVoter accessVoter;

  // Use cases
  private final ProductGroupSearchUseCase productGroupSearchUseCase;
  private final ProductSearchUseCase productSearchUseCase;

  /**
   * List groups.
   */
  @GetMapping("/listGroups")
  @Operation(
      operationId = "listProductGroups",
      summary = "List product groups",
      description = "Retrieve product groups, optionally filtered by parent group ID"
  )
  public ProductListResponse listGroups(
      @RequestParam(value = "parentGroupId", required = false) @Nullable Integer parentGroupId) {
    accessVoter.assertCanListGroups();
    return productGroupSearchUseCase.execute(parentGroupId);
  }

  /**
   * List products.
   */
  @GetMapping("/listProducts")
  @Operation(
      operationId = "listProducts",
      summary = "List products",
      description = "Retrieve products filtered by group ID, with optional brand and name filters"
  )
  public ProductListResponse listProducts(
      @RequestParam("groupId") @NonNull Integer groupId,
      @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
      @RequestParam(value = "name", required = false) @Nullable String nameLike) {
    accessVoter.assertCanListProducts();
    return productSearchUseCase.execute(groupId, brandId, nameLike);
  }

  /**
   * Search products.
   */
  @GetMapping("/search")
  @Operation(
      operationId = "searchProducts",
      summary = "Search products",
      description = "Search for products with optional filters for group, brand, and name"
  )
  public ProductListResponse search(
      @RequestParam(value = "groupId", required = false) @Nullable Integer groupId,
      @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
      @RequestParam(value = "name", required = false) @Nullable String nameLike) {
    accessVoter.assertCanSearch();
    return productSearchUseCase.execute(groupId, brandId, nameLike);
  }
}