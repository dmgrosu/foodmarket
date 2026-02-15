package md.ramaiana.foodmarket.domain.order.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.enums.OrderState;

/**
 * Order Entity.
 */
@Getter
@Setter
@Entity
@Table(name = "\"order\"")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

  @NonNull
  @Setter(AccessLevel.NONE)
  private final UUID uuid = UUID.randomUUID();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  private ClientEntity client;

  @NonNull
  @Column(name = "total_sum")
  private Float totalSum = 0f;

  @NonNull
  @Setter(AccessLevel.NONE)
  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @Nullable
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Nullable
  @Column(name = "processed_at")
  private Instant processedAt;

  @Nullable
  @Column(name = "processing_result")
  private String processingResult;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private OrderState state = OrderState.NEW;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrderItemEntity> items = new ArrayList<>();

  /**
   * Constructor.
   */
  public OrderEntity(@NonNull ClientEntity client) {
    this.client = client;
  }

  public void addItem(@NonNull OrderItemEntity item) {
    items.add(item);
    item.setOrder(this);
    updateTotalSum();
  }

  public void removeItem(@NonNull OrderItemEntity item) {
    items.remove(item);
    item.setOrder(null);
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
