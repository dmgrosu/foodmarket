package md.ramaiana.foodmarket.domain.product.data;

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
 * Paged search for {@link ProductEntity}.
 * <p>
 * A custom fragment rather than a derived or {@code @Query} finder: Spring Data JDBC rejects
 * {@code Page} return types on {@code @Query} methods, and derived finders cannot express the
 * three optional filters. Going through the aggregate operations also loads each product's
 * {@code prices} child collection.
 * <p>
 * Unlike {@code findAllByFiltersHavingPositiveBalance}, this does not join {@code balances} —
 * an admin listing must show zero-stock products too.
 * <p>
 * Paging is assembled from {@code findAll} + {@code count} rather than the
 * {@code findAll(Query, Class, Pageable)} overload, which is deprecated for removal in 4.0.
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

  private final JdbcAggregateOperations aggregateOperations;

  @Override
  @NonNull
  public Page<ProductEntity> search(@Nullable String nameLike, @Nullable Integer brandId, @Nullable Integer groupId,
                                    @NonNull Pageable pageable) {
    Criteria criteria = Criteria.where("deletedAt").isNull();

    if (nameLike != null && !nameLike.isBlank()) {
      criteria = criteria.and("name").like("%" + nameLike + "%").ignoreCase(true);
    }
    if (brandId != null) {
      criteria = criteria.and("brandId").is(brandId);
    }
    if (groupId != null) {
      criteria = criteria.and("groupId").is(groupId);
    }

    Query query = Query.query(criteria);

    return PageableExecutionUtils.getPage(
        aggregateOperations.findAll(query.with(pageable), ProductEntity.class),
        pageable,
        () -> aggregateOperations.count(query, ProductEntity.class));
  }
}
