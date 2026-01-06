package md.ramaiana.foodmarket.service;

import lombok.NonNull;
import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.dao.ProductDao;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderState;
import md.ramaiana.foodmarket.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final ClientDao clientDao;

    @Autowired
    public OrderService(OrderDao orderDao,
                        ProductDao productDao,
                        ClientDao clientDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.clientDao = clientDao;
    }

    public Order findOrdersById(Integer orderId) throws OrderNotFoundException, OrderIdZeroException, IllegalArgumentException {
        return orderDao.findByIdAndDeletedAtNull(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Order with ID [%s] not found", orderId)));
    }

    public void deleteOrderById(Integer orderId) {
        orderDao.setOrderToDeletedState(orderId);
    }

    @Transactional
    public Order addProductToOrder(int orderId, int productId,
                                   float quantity, int clientId) throws ProductNotFoundException, ClientNotFoundException, OrderAlreadyProcessedException, OrderNotFoundException {
        Product product = findProduct(productId);
        validateClient(clientId);
        Order order = findOrCreate(orderId, clientId);
        order.addProduct(product, quantity);
        return orderDao.save(order);
    }

    public Page<@NonNull Order> findOrdersByPeriod(OffsetDateTime from, OffsetDateTime to, Integer clientId,
                                                   Integer page, Integer pageSize, String direction, String column) throws ClientNotFoundException {
        validateClient(clientId);
        PageRequest pageable = PageRequest.of(page, pageSize, Sort.Direction.valueOf(direction), column);
        return orderDao.findAllByDeletedAtNullAndCreatedAtBetweenAndClient(pageable, from, to, clientId);
    }

    public void updateProductQuantity(int orderId, int productId, float newQuantity) throws OrderNotFoundException {
        Order order = findOrder(orderId);
        orderDao.save(order.updateQuantity(productId, newQuantity));
    }

    public void deleteProductFromOrder(int orderId, int orderItemId) throws OrderNotFoundException {
        Order order = findOrder(orderId);
        order.removeProduct(orderItemId);
        orderDao.save(order);
    }

    public void placeOrder(int orderId) throws OrderNotFoundException {
        if (!orderDao.existsByIdAndDeletedAtNull(orderId)) {
            throw new OrderNotFoundException(String.format("Order with id [%s] not found", orderId));
        }
        orderDao.updateOrderState(OrderState.PLACED, orderId);
    }

    private void validateClient(Integer clientId) throws ClientNotFoundException {
        clientDao.findByIdAndDeletedAtNull(clientId)
                .orElseThrow(() -> new ClientNotFoundException(String.format("Client with ID [%s] not found", clientId)));
    }

    private Product findProduct(Integer productId) throws ProductNotFoundException {
        return productDao.findByIdAndDeletedAtNull(productId)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with ID [%s] not found", productId)));
    }

    private Order findOrCreate(int orderId, int clientId) throws OrderNotFoundException, OrderAlreadyProcessedException {
        Order order = orderId != 0 ?
                findOrder(orderId) :
                Order.builder()
                        .client(AggregateReference.to(clientId))
                        .state(OrderState.NEW)
                        .build();
        if (order.getState() == OrderState.PROCESSED) {
            throw new OrderAlreadyProcessedException(String.format("Order with ID [%s] has been already processed", orderId));
        }
        if (order.getState() == OrderState.PLACED) {
            throw new OrderAlreadyProcessedException(String.format("Order with ID [%s] has been already placed", orderId));
        }
        return order;
    }

    private Order findOrder(int orderId) throws OrderNotFoundException {
        return orderDao.findByIdAndDeletedAtNull(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Order with ID [%s] not found", orderId)));
    }

}
