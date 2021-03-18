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
        Order order = orderDao.getByIdAndDeletedAtNull(orderId);
        if (order == null) {
            throw new OrderNotFoundException(String.format("Order with ID [%s] not found", orderId));
        } else {
            return order;
        }
    }


    public void deleteOrderById(Integer orderId) {
        orderDao.setOrderToDeletedState(orderId);
    }

    public Order addGoodToOrder(Integer orderId, Integer goodId, Float quantity, Integer clientId) throws GoodNotFoundException,
            ClientNotFoundException {
        validateGood(goodId);
        validateClient(clientId);
        Float sum = goodDao.getByIdAndDeletedAtNull(goodId).getPrice() * quantity;
        return orderDao.save(Order.builder()
                .id(orderId)
                .clientId(clientId)
                .totalSum(sum)
                .build());
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

    private void validateGood(Integer goodId) throws GoodNotFoundException {
        Good good = goodDao.getByIdAndDeletedAtNull(goodId);
        if (good == null) {
            throw new GoodNotFoundException(String.format("Good with ID [%s] not found", goodId));
        }
    }

}
