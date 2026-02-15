package md.ramaiana.foodmarket.domain.brand.data;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Brand Repository.
 */
@Repository
public interface BrandRepository extends
    JpaRepository<@NonNull BrandEntity, @NonNull Integer>,
    JpaSpecificationExecutor<@NonNull BrandEntity> {
}
