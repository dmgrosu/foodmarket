package md.ramaiana.foodmarket.domain.order.data;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;

/**
 * Order Item Entity.
 */
@Getter
@Setter
@Entity
@Table(name = "order_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity {

  @NonNull
  @Setter(AccessLevel.NONE)
  private final UUID uuid = UUID.randomUUID();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private ProductEntity product;

  @NonNull
  private Float quantity;

  @NonNull
  private Float price;

  @NonNull
  private Float sum;

  @NonNull
  private Float weight;

  /**
   * Constructor.
   */
  public OrderItemEntity(@NonNull ProductEntity product, @NonNull Float quantity) {
    this.product = product;
    this.quantity = quantity;
    this.price = product.getPrice();
    this.sum = product.getPrice() * quantity;
    this.weight = (product.getWeight() != null ? product.getWeight() : 0f) * quantity;
  }

  public void updateQuantity(@NonNull Float newQuantity) {
    this.quantity = newQuantity;
    this.sum = this.price * newQuantity;
    this.weight = (this.product.getWeight() != null ? this.product.getWeight() : 0f) * newQuantity;
  }
}
