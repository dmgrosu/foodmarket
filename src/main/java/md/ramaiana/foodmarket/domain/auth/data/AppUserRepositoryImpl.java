package md.ramaiana.foodmarket.domain.auth.data;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Paged search for {@link AppUserEntity}.
 * <p>
 * A custom fragment rather than a derived or {@code @Query} finder: Spring Data JDBC rejects
 * {@code Page} return types on {@code @Query} methods, and derived finders cannot express an
 * optional filter. Going through the aggregate operations also loads each user's {@code userRoles}
 * child collection. Mirrors the paged-search pattern this repo used for clients before that screen
 * was retired.
 * <p>
 * Paging is assembled from {@code findAll} + {@code count} rather than the
 * {@code findAll(Query, Class, Pageable)} overload, which is deprecated for removal in 4.0.
 */
@Repository
@RequiredArgsConstructor
public class AppUserRepositoryImpl implements AppUserRepositoryCustom {

  private final JdbcAggregateOperations aggregateOperations;

  @Override
  @NonNull
  public Page<AppUserEntity> search(@Nullable String emailLike, @Nullable UserState state, @NonNull Pageable pageable) {
    Criteria criteria = Criteria.empty();

    if (emailLike != null && !emailLike.isBlank()) {
      criteria = criteria.and("email").like("%" + emailLike + "%").ignoreCase(true);
    }
    if (state != null) {
      criteria = criteria.and("state").is(state);
    }

    Query query = Query.query(criteria);

    return PageableExecutionUtils.getPage(
        aggregateOperations.findAll(query.with(pageable), AppUserEntity.class),
        pageable,
        () -> aggregateOperations.count(query, AppUserEntity.class));
  }
}
