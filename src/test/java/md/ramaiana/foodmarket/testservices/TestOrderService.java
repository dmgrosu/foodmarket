package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestOrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  public TestOrderService(OrderRepository orderRepository, ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
  }

  @Transactional
  public OrderEntity create(Integer clientId) {
    var order = new OrderEntity(clientId);
    return orderRepository.save(order);
  }

  @Transactional
  public OrderEntity createWithItems(Integer clientId, Integer... productIds) {
    var order = new OrderEntity(clientId);
    for (Integer productId : productIds) {
      ProductEntity product = productRepository.findById(productId)
          .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
      float unitWeight = product.getWeight() != null ? product.getWeight() : 0f;
      var item = new OrderItemEntity(productId, product.getPrice(), unitWeight, 1f);
      order.addItem(item);
    }
    return orderRepository.save(order);
  }
}
