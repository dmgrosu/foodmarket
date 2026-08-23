package md.ramaiana.foodmarket.domain.brand.data;

import org.springframework.data.repository.ListCrudRepository;

/**
 * Brand Repository.
 */
public interface BrandRepository extends ListCrudRepository<BrandEntity, Integer>, BrandRepositoryCustom {

}
