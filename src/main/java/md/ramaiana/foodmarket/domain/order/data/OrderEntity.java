package md.ramaiana.foodmarket.domain.order.data;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

// TODO: make all props final
// TODO: add PersistenceCreator constructor

/**
 * Order Entity. Also serves as the cart: a client's single {@link OrderState#NEW} order is their cart.
 * <p>
 * An order is priced against exactly one storage and one price tier, both fixed when the order is
 * created, because {@code prices} is keyed by (product, storage, type) and a line has no meaning
 * without them.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("order")
public class OrderEntity {

  @Id
  private Integer id;
  private Integer clientId;
  private Integer storageId;
  private PriceType priceType;
  @NonNull
  private Float totalSum = 0f;
  @NonNull
  private Instant createdAt = Instant.now();
  @Nullable
  private Instant placedAt;
  @Nullable
  private Instant deletedAt;
  @Nullable
  private Instant processedAt;
  @Nullable
  private String processingResult;
  @Nullable
  private Instant exportedAt;
  @NonNull
  @Column("status")
  private OrderState state = OrderState.NEW;
  @MappedCollection(idColumn = "order_id")
  private Set<OrderItemEntity> items = new HashSet<>();

  /**
   * Constructor.
   */
  public OrderEntity(@NonNull Integer clientId, @NonNull Integer storageId, @NonNull PriceType priceType) {
    this.clientId = clientId;
    this.storageId = storageId;
    this.priceType = priceType;
  }

  /**
   * Add {@code quantity} of a product, merging into the line that already holds it if there is one.
   * A merged line keeps the price it was added at.
   */
  public void addProduct(@NonNull Integer productId, float price, float unitWeight, float quantity) {
    findItem(productId).ifPresentOrElse(
        item -> item.updateQuantity(item.getQuantity() + quantity, unitWeight),
        () -> items.add(new OrderItemEntity(productId, price, unitWeight, quantity)));
    updateTotalSum();
  }

  /**
   * Set the quantity of a line outright, in contrast to {@link #addProduct} which adds to it.
   *
   * @return false if the order holds no line for that product.
   */
  public boolean setProductQuantity(@NonNull Integer productId, float quantity, float unitWeight) {
    Optional<OrderItemEntity> item = findItem(productId);
    item.ifPresent(it -> it.updateQuantity(quantity, unitWeight));
    updateTotalSum();
    return item.isPresent();
  }

  /**
   * Remove the line holding a product.
   *
   * @return false if the order holds no line for that product.
   */
  public boolean removeProduct(@NonNull Integer productId) {
    Optional<OrderItemEntity> item = findItem(productId);
    item.ifPresent(items::remove);
    updateTotalSum();
    return item.isPresent();
  }

  /**
   * The line holding a product, if the order has one. A product appears at most once: {@link #addProduct}
   * merges rather than appending.
   */
  @NonNull
  public Optional<OrderItemEntity> findItem(@NonNull Integer productId) {
    return items.stream()
        .filter(item -> productId.equals(item.getProductId()))
        .findFirst();
  }

  public void clearItems() {
    items.clear();
    updateTotalSum();
  }

  public boolean isEmpty() {
    return items.isEmpty();
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

  /**
   * Hand the cart over as an order. {@code createdAt} is when the cart was opened, which can be days
   * earlier, so the moment of placement is recorded separately — it is the order date the ERP sees.
   */
  public void place() {
    this.state = OrderState.PLACED;
    this.placedAt = Instant.now();
  }

  public void markDeleted() {
    this.deletedAt = Instant.now();
  }
}
