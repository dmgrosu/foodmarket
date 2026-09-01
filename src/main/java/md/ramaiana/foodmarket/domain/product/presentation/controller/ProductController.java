package md.ramaiana.foodmarket.domain.product.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductGroupResponse;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductGroupSearchUseCase;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductSearchCriteria;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductSearchUseCase;
import md.ramaiana.foodmarket.domain.product.presentation.voter.ProductAccessVoter;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product controller.
 */
@Validated
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    /**
     * Upper bound on {@code pageSize}. The catalogue runs to thousands of products, each carrying
     * its prices, so an uncapped page is a way to ask for the whole thing in one response.
     */
    private static final long MAX_PAGE_SIZE = 200;

    // Access voters
    private final ProductAccessVoter accessVoter;

    // Use cases
    private final ProductGroupSearchUseCase productGroupSearchUseCase;
    private final ProductSearchUseCase productSearchUseCase;

    /**
     * List the non-empty groups under a parent, or the roots when no parent is given.
     */
    @GetMapping("/listGroups")
    public List<ProductGroupResponse> listGroups(
            @RequestParam(value = "storageId", required = false) @Nullable Integer storageId,
            @RequestParam(value = "parentGroupId", required = false) @Nullable Integer parentGroupId) {
        accessVoter.assertCanListGroups();
        return productGroupSearchUseCase.execute(storageId, parentGroupId);
    }

    /**
     * Search the catalogue, a page at a time. Every filter is optional: with a {@code groupId} this
     * is the listing for that group, without one it searches across the whole catalogue.
     * <p>
     * It replaced a separate {@code /listProducts} that differed only in requiring {@code groupId}.
     * That requirement was standing in for a size limit, and paging does the job properly.
     */
    @GetMapping("/search")
    public PagedResponse<ProductResponse> search(
            @RequestParam(value = "storageId", required = false) @Nullable Integer storageId,
            @RequestParam(value = "groupId", required = false) @Nullable Integer groupId,
            @RequestParam(value = "brandId", required = false) @Nullable Integer brandId,
            @RequestParam(value = "name", required = false) @Nullable String nameLike,
            @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "25") @Min(1) @Max(MAX_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortColumn", defaultValue = "name") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection) {
        accessVoter.assertCanSearch();
        return productSearchUseCase.execute(new ProductSearchCriteria(
                storageId, groupId, brandId, nameLike, pageNo, pageSize, sortColumn, sortDirection));
    }
}
