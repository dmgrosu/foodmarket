package md.ramaiana.foodmarket.domain.order.data;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

// TODO: make all props final
// TODO: add PersistenceCreator constructor
// TODO: add storage entity ref

@Getter
@Setter
@NoArgsConstructor
@Table("order")
public class OrderEntity {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  private Integer clientId;
  @NonNull
  private Float totalSum = 0f;
  @NonNull
  private Instant createdAt = Instant.now();
  @Nullable
  private Instant deletedAt;
  @Nullable
  private Instant processedAt;
  @Nullable
  private String processingResult;
  @NonNull
  @Column("status")
  private OrderState state = OrderState.NEW;
  @MappedCollection(idColumn = "order_id")
  private Set<OrderItemEntity> items = new HashSet<>();

  /**
   * Constructor.
   */
  public OrderEntity(@NonNull Integer clientId) {
    this.clientId = clientId;
  }

  public void addItem(@NonNull OrderItemEntity item) {
    items.add(item);
    updateTotalSum();
  }

  public void removeItem(@NonNull OrderItemEntity item) {
    items.remove(item);
    updateTotalSum();
  }

  public void updateTotalSum() {
    this.totalSum = items.stream()
        .map(OrderItemEntity::getSum)
        .reduce(0f, Float::sum);
  }

  public float getTotalWeightForProducts() {
    return items.stream()
        .map(OrderItemEntity::getWeight)
        .reduce(0f, Float::sum);
  }

  public void markDeleted() {
    this.deletedAt = Instant.now();
  }
}
