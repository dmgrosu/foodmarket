package md.ramaiana.foodmarket.domain.product.data;

import jakarta.annotation.Nullable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Paged and grouped lookups over {@link ProductEntity}.
 * <p>
 * A custom fragment rather than a derived or {@code @Query} finder: Spring Data JDBC rejects
 * {@code Page} return types on {@code @Query} methods, and {@code Criteria} cannot express the
 * stock condition, which reaches into another table.
 * <p>
 * A page is read in two steps — ids first, then the aggregates for those ids. Selecting whole rows
 * under a {@code LIMIT} would work, but the aggregates have to come back through
 * {@link JdbcAggregateOperations} anyway for their {@code prices} collection to be populated, and
 * that takes ids.
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

  /**
   * Sort property to column. The sort arrives from an HTTP parameter and is interpolated into the
   * SQL, so it is resolved through this map and never taken from the request as-is.
   */
  private static final Map<String, String> SORT_COLUMNS = Map.of(
      "id", "id",
      "name", "name",
      "weight", "weight",
      "barCode", "bar_code");

  private static final String FILTERS = """
      FROM product p
      WHERE p.deleted_at IS NULL
        AND (:groupId IS NULL OR p.group_id = :groupId)
        AND (:brandId IS NULL OR p.brand_id = :brandId)
        AND (:nameLike IS NULL OR LOWER(p.name) LIKE :nameLike)
        AND EXISTS (SELECT 1 FROM balances b
                     WHERE b.product_id = p.id
                       AND b.quantity > 0
                       AND (:storageId IS NULL OR b.storage_id = :storageId))
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final JdbcAggregateOperations aggregateOperations;

  @Override
  @NonNull
  public Page<ProductEntity> searchInStock(@Nullable Integer storageId, @Nullable Integer groupId,
                                           @Nullable Integer brandId, @Nullable String nameLike,
                                           @NonNull Pageable pageable) {
    MapSqlParameterSource parameters = filterParameters(storageId, groupId, brandId, nameLike)
        .addValue("limit", pageable.getPageSize(), Types.INTEGER)
        .addValue("offset", pageable.getOffset(), Types.BIGINT);

    List<Integer> ids = jdbcTemplate.queryForList(
        "SELECT p.id " + FILTERS + orderBy(pageable.getSort()) + " LIMIT :limit OFFSET :offset",
        parameters, Integer.class);

    List<ProductEntity> products = loadInOrder(ids);

    return PageableExecutionUtils.getPage(products, pageable, () -> {
      Long total = jdbcTemplate.queryForObject("SELECT count(*) " + FILTERS,
          filterParameters(storageId, groupId, brandId, nameLike), Long.class);
      return total == null ? 0 : total;
    });
  }

  @Override
  @NonNull
  public List<Integer> findGroupIdsInStock(@Nullable Integer storageId, @Nullable Integer groupId,
                                           @Nullable Integer brandId, @Nullable String nameLike) {
    return jdbcTemplate.queryForList(
        "SELECT DISTINCT p.group_id " + FILTERS + " AND p.group_id IS NOT NULL",
        filterParameters(storageId, groupId, brandId, nameLike), Integer.class);
  }

  /**
   * Loads the aggregates for {@code ids} and puts them back into the order the page query
   * established — {@code findAllById} makes no promise about ordering.
   */
  @NonNull
  private List<ProductEntity> loadInOrder(@NonNull List<Integer> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    Map<Integer, ProductEntity> byId = new LinkedHashMap<>();
    for (ProductEntity product : aggregateOperations.findAllById(ids, ProductEntity.class)) {
      byId.put(product.getId(), product);
    }
    List<ProductEntity> ordered = new ArrayList<>(ids.size());
    for (Integer id : ids) {
      ProductEntity product = byId.get(id);
      if (product != null) {
        ordered.add(product);
      }
    }
    return ordered;
  }

  /**
   * Every filter is bound with an explicit SQL type. An untyped {@code null} leaves the driver
   * unable to resolve the {@code IS NULL} comparisons above.
   */
  @NonNull
  private MapSqlParameterSource filterParameters(@Nullable Integer storageId, @Nullable Integer groupId,
                                                 @Nullable Integer brandId, @Nullable String nameLike) {
    return new MapSqlParameterSource()
        .addValue("storageId", storageId, Types.INTEGER)
        .addValue("groupId", groupId, Types.INTEGER)
        .addValue("brandId", brandId, Types.INTEGER)
        .addValue("nameLike", toLikePattern(nameLike), Types.VARCHAR);
  }

  @Nullable
  private String toLikePattern(@Nullable String nameLike) {
    return nameLike == null || nameLike.isBlank() ? null : "%" + nameLike.toLowerCase() + "%";
  }

  /**
   * Always ends on {@code p.id} so the ordering is total. Paging over a column with ties — product
   * names repeat across brands — otherwise lets a row appear on two pages, or on none.
   */
  @NonNull
  private String orderBy(@NonNull Sort sort) {
    StringBuilder orderBy = new StringBuilder(" ORDER BY ");
    for (Sort.Order order : sort) {
      String column = SORT_COLUMNS.get(order.getProperty());
      if (column == null) {
        throw new IllegalArgumentException("Unsortable product property: " + order.getProperty());
      }
      orderBy.append("p.").append(column).append(order.isAscending() ? " ASC, " : " DESC, ");
    }
    return orderBy.append("p.id ASC").toString();
  }
}
