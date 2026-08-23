package md.ramaiana.foodmarket.domain.brand.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Paged search for {@link BrandEntity}.
 * <p>
 * A custom fragment rather than a derived or {@code @Query} finder: Spring Data JDBC rejects
 * {@code Page} return types on {@code @Query} methods, and derived finders cannot express an
 * optional filter. The criteria API covers both, and still maps property names through the
 * mapping context, so sorting and filtering stay type-safe.
 * <p>
 * Paging is assembled from {@code findAll} + {@code count} rather than the
 * {@code findAll(Query, Class, Pageable)} overload, which is deprecated for removal in 4.0.
 */
@Repository
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepositoryCustom {

  private final JdbcAggregateOperations aggregateOperations;

  @Override
  @NonNull
  public Page<BrandEntity> search(@Nullable String nameLike, @NonNull Pageable pageable) {
    Criteria criteria = Criteria.where("deletedAt").isNull();

    if (nameLike != null && !nameLike.isBlank()) {
      criteria = criteria.and("name").like("%" + nameLike + "%").ignoreCase(true);
    }

    Query query = Query.query(criteria);

    return PageableExecutionUtils.getPage(
        aggregateOperations.findAll(query.with(pageable), BrandEntity.class),
        pageable,
        () -> aggregateOperations.count(query, BrandEntity.class));
  }
}
