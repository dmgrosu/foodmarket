package md.ramaiana.foodmarket.domain.product.presentation.controller;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductListResponse;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductGroupSearchUseCase;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductSearchCriteria;
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
    public ProductListResponse listGroups(
            @RequestParam(value = "storageId", required = false) @Nullable Integer storageId,
            @RequestParam(value = "parentGroupId", required = false) @Nullable Integer parentGroupId) {
        accessVoter.assertCanListGroups();
        return productGroupSearchUseCase.execute(storageId, parentGroupId);
    }

    /**
     * List products.
     */
    @GetMapping("/listProducts")
    public ProductListResponse listProducts(@RequestParam(value = "storageId", required = false) @Nullable Integer storageId,
                                            @RequestParam("groupId") @NonNull Integer groupId,
                                            @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
                                            @RequestParam(value = "name", required = false) @Nullable String nameLike) {
        accessVoter.assertCanListProducts();
        ProductSearchCriteria criteria = new ProductSearchCriteria(storageId, groupId, brandId, nameLike);
        return productSearchUseCase.execute(criteria);
    }

    /**
     * Search products.
     */
    @GetMapping("/search")
    public ProductListResponse search(
            @RequestParam(value = "storageId", required = false) @Nullable Integer storageId,
            @RequestParam(value = "groupId", required = false) @Nullable Integer groupId,
            @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
            @RequestParam(value = "name", required = false) @Nullable String nameLike) {
        accessVoter.assertCanSearch();
        ProductSearchCriteria criteria = new ProductSearchCriteria(storageId, groupId, brandId, nameLike);
        return productSearchUseCase.execute(criteria);
    }
}