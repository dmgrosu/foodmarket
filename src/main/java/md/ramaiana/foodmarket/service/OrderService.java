package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.dao.OrderGoodDao;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * @author Grosu Kirill (grosukirill009@gmail.com), 2/12/2021
 **/

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final GoodDao goodDao;
    private final ClientDao clientDao;
    private final OrderGoodDao orderGoodDao;

    @Autowired
    public OrderService(OrderDao orderDao,
                        GoodDao goodDao,
                        ClientDao clientDao,
                        OrderGoodDao orderGoodDao) {
        this.orderDao = orderDao;
        this.goodDao = goodDao;
        this.clientDao = clientDao;
        this.orderGoodDao = orderGoodDao;
    }

    public Order findOrdersById(Integer orderId) throws OrderNotFoundException, OrderIdZeroException, IllegalArgumentException {
        Assert.notNull(orderId, "Order ID is null");
        if (orderId == 0) {
            throw new OrderIdZeroException("Order ID is zero");
        }
        return getOrderById(orderId);
    }

    public void deleteOrderById(Integer orderId) {
        orderDao.setOrderToDeletedState(orderId);
    }

    //@Transactional
    public Order addGoodToOrder(int orderId, int goodId, float quantity, int clientId) throws GoodNotFoundException,
            ClientNotFoundException, OrderAlreadyProcessedException, OrderNotFoundException {
        Good good = validateGood(goodId);
        validateClient(clientId);
        if (orderId == 0) {
            Order savedOrder = orderDao.save(Order.builder()
                    .clientId(clientId)
                    .totalSum(good.getPrice() * quantity)
                    .createdAt(OffsetDateTime.now(ZoneId.of("UTC")))
                    .build());
            orderId = savedOrder.getId();
        } else {
            if (!orderDao.existsByIdAndDeletedAtNull(orderId)) {
                throw new OrderNotFoundException(String.format("Order with ID [%s] not found", orderId));
            } else {
                validateOrderState(orderId);
            }
        }
        createOrUpdateOrderGood(orderId, good, quantity);
        return updateTotalSumAndSaveOrder(orderId);
    }

    private void createOrUpdateOrderGood(int orderId, Good good, float quantity) {
        if (orderGoodDao.existsByOrderIdAndGoodId(orderId, good.getId())) {
            orderGoodDao.updateOrderGoodQuantity(orderId, good.getId(), quantity);
        } else {
            orderGoodDao.save(OrderGood.builder()
                    .goodId(good.getId())
                    .orderId(orderId)
                    .quantity(quantity)
                    .sum(good.getPrice() * quantity)
                    .weight(good.getWeight() * quantity)
                    .build());
        }
    }

    public Page<Order> findOrdersByPeriod(OffsetDateTime from, OffsetDateTime to, Integer clientId, Integer page, Integer pageSize, String direction, String column) throws ClientNotFoundException {
        validateClient(clientId);
        PageRequest pageable = PageRequest.of(page, pageSize, Sort.Direction.valueOf(direction), column);
        return orderDao.findAllByDeletedAtNullAndCreatedAtBetweenAndClientId(pageable, from, to, clientId);
    }

    public void updateOrder(int orderId, int goodId, float newQuantity) throws GoodNotFoundException {
        validateGood(goodId);
        orderGoodDao.updateOrderGoodQuantity(orderId, goodId, newQuantity);
    }

    private void validateClient(Integer clientId) throws ClientNotFoundException {
        Client client = clientDao.getByIdAndDeletedAtNull(clientId);
        if (client == null) {
            throw new ClientNotFoundException(String.format("Client with ID [%s] not found", clientId));
        }
    }

    private Good validateGood(Integer goodId) throws GoodNotFoundException {
        Good good = goodDao.getByIdAndDeletedAtNull(goodId);
        if (good == null) {
            throw new GoodNotFoundException(String.format("Good with ID [%s] not found", goodId));
        }
        return good;
    }

    private void validateOrderState(int orderId) throws OrderAlreadyProcessedException {
        String processingResult = orderDao.getProcessingResultById(orderId);
        if (processingResult != null) {
            throw new OrderAlreadyProcessedException(String.format("Order with ID [%s] has been already processed", orderId));
        }
    }

    private Order getOrderById(int orderId) throws OrderNotFoundException {
        return orderDao.findByIdAndDeletedAtNull(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Order with ID [%s] not found", orderId)));
    }

    private Order updateTotalSumAndSaveOrder(Integer orderId) throws OrderNotFoundException {
        Order order = getOrderById(orderId);
        order.updateTotalSum();
        return orderDao.save(order);
    }

}
