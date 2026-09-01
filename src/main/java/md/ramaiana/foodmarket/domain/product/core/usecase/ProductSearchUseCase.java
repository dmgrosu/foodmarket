package md.ramaiana.foodmarket.domain.product.core.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for listing products, a page at a time.
 */
@UseCase
@RequiredArgsConstructor
public class ProductSearchUseCase {

    /**
     * Sort is a free-text request parameter, so it is constrained to an explicit set of
     * entity properties rather than passed straight to the persistence layer.
     */
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "name", "weight", "barCode");

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;

    /**
     * Execute the use case.
     */
    @NonNull
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> execute(@NonNull ProductSearchCriteria criteria) {
        if (!SORTABLE_PROPERTIES.contains(criteria.sortColumn())) {
            throw new BadRequestException(String.format("Unknown sort column '%s'", criteria.sortColumn()));
        }

        Pageable pageable = PageRequest.of(criteria.pageNo(), criteria.pageSize(),
                Sort.by(criteria.sortDirection(), criteria.sortColumn()));

        Page<ProductEntity> page = productRepository.searchInStock(
                criteria.storageId(), criteria.groupId(), criteria.brandId(),
                criteria.nameLike(), pageable);

        Map<Integer, String> groupNames = groupNamesFor(page.getContent());

        return new PagedResponse<>(page.map(
                product -> new ProductResponse(product, groupNames.get(product.getGroupId()))));
    }

    /**
     * One lookup for the whole page rather than one per row.
     */
    private Map<Integer, String> groupNamesFor(List<ProductEntity> products) {
        Set<Integer> groupIds = products.stream()
                .map(ProductEntity::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> namesById = new HashMap<>();
        for (ProductGroupEntity group : productGroupRepository.findAllById(groupIds)) {
            namesById.put(group.getId(), group.getName());
        }
        return namesById;
    }
}
