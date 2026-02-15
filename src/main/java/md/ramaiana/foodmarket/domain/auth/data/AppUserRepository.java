package md.ramaiana.foodmarket.domain.auth.data;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


/**
 * AppUser Repository.
 */
@Repository
public interface AppUserRepository extends JpaRepository<@NonNull AppUserEntity, @NonNull Integer>, JpaSpecificationExecutor<@NonNull AppUserEntity> {

  /**
   * Specification for finding Brand by ERP Code.
   */
  @NonNull
  static Specification<@NonNull AppUserEntity> emailEquals(@NonNull String input) {
    return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), input);
  }
}
