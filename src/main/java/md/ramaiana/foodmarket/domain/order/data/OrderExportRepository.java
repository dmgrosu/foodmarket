package md.ramaiana.foodmarket.domain.order.data;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads orders in the shape the ERP export needs, and marks them handed over.
 * <p>
 * Hand-written rather than derived, following {@code BalanceRepository}: the export is keyed by the
 * ERP's codes, not ours, so every row needs {@code client.erp_code}, {@code storages.erp_code} and
 * {@code product.erp_code} joined in. Reading the aggregates and resolving those per order would be
 * a query per order per line; this is two queries for a whole batch.
 */
@Repository
@RequiredArgsConstructor
public class OrderExportRepository {

  private static final String SELECT_PENDING_ORDERS = """
      SELECT o.id, o.created_at, o.placed_at, o.price_type, o.total_sum,
             c.erp_code AS client_erp_code, c.idno AS client_idno,
             s.erp_code AS storage_erp_code
      FROM "order" o
      JOIN client c ON c.id = o.client_id
      LEFT JOIN storages s ON s.id = o.storage_id
      WHERE o.status = :status AND o.deleted_at IS NULL
      ORDER BY o.id
      """;

  private static final String SELECT_PENDING_ITEMS = """
      SELECT i.order_id, i.quantity, i.price, i.sum AS line_sum, i.weight,
             p.erp_code AS product_erp_code
      FROM order_product i
      JOIN product p ON p.id = i.product_id
      WHERE i.order_id IN (:orderIds)
      ORDER BY i.order_id, i.id
      """;

  private static final String MARK_EXPORTED = """
      UPDATE "order" SET status = :status, exported_at = :exportedAt
      WHERE id IN (:ids)
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * One row per order awaiting hand-over. {@code storageErpCode} is null when the order names a
   * storage the ERP no longer sends.
   */
  @NonNull
  @Transactional(readOnly = true)
  public List<PendingOrderRow> findPendingOrders() {
    return jdbcTemplate.query(
        SELECT_PENDING_ORDERS,
        new MapSqlParameterSource("status", OrderState.PLACED.name()),
        (row, index) -> new PendingOrderRow(
            row.getInt("id"),
            toInstant(row.getTimestamp("created_at")),
            toInstant(row.getTimestamp("placed_at")),
            row.getString("client_erp_code"),
            // client.idno is char(13) in production, so it comes back space-padded.
            trimmed(row.getString("client_idno")),
            row.getString("storage_erp_code"),
            row.getString("price_type"),
            row.getFloat("total_sum")));
  }

  /**
   * The lines of the given orders, ordered by order then by line.
   */
  @NonNull
  @Transactional(readOnly = true)
  public List<PendingItemRow> findPendingItems(@NonNull Collection<Integer> orderIds) {
    if (orderIds.isEmpty()) {
      return List.of();
    }

    return jdbcTemplate.query(
        SELECT_PENDING_ITEMS,
        new MapSqlParameterSource("orderIds", orderIds),
        (row, index) -> new PendingItemRow(
            row.getInt("order_id"),
            row.getString("product_erp_code"),
            row.getFloat("quantity"),
            row.getFloat("price"),
            row.getFloat("line_sum"),
            row.getFloat("weight")));
  }

  /**
   * Flip the given orders to {@link OrderState#EXPORTED}.
   * <p>
   * A targeted update, deliberately not {@code orderRepository.save(order)}: saving the aggregate
   * would delete and re-insert every {@code order_product} row just to change a status column.
   *
   * @return how many orders were updated.
   */
  @Transactional(rollbackFor = Exception.class)
  public int markExported(@NonNull Collection<Integer> orderIds, @NonNull Instant exportedAt) {
    if (orderIds.isEmpty()) {
      return 0;
    }

    return jdbcTemplate.update(MARK_EXPORTED, new MapSqlParameterSource()
        .addValue("status", OrderState.EXPORTED.name())
        .addValue("exportedAt", java.sql.Timestamp.from(exportedAt))
        .addValue("ids", orderIds));
  }

  private Instant toInstant(java.sql.Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }

  private String trimmed(String value) {
    return value == null ? null : value.trim();
  }

  /**
   * An order ready to hand over, already resolved to the codes the ERP knows it by.
   */
  public record PendingOrderRow(
      Integer id,
      Instant createdAt,
      Instant placedAt,
      String clientErpCode,
      String clientIdno,
      String storageErpCode,
      String priceType,
      Float totalSum) {
  }

  /**
   * A single line of a {@link PendingOrderRow}.
   */
  public record PendingItemRow(
      Integer orderId,
      String productErpCode,
      Float quantity,
      Float price,
      Float sum,
      Float weight) {
  }
}
