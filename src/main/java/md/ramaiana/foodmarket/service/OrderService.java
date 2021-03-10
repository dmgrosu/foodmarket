package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Grosu Kirill (grosukirill009@gmail.com), 2/12/2021
 **/

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final GoodDao goodDao;
    private final ClientDao clientDao;

    @Autowired
    public OrderService(OrderDao orderDao,
                        GoodDao goodDao,
                        ClientDao clientDao) {
        this.orderDao = orderDao;
        this.goodDao = goodDao;
        this.clientDao = clientDao;
    }

    public Order findOrdersById(Integer orderId) {
        return orderDao.getByIdAndDeletedAtNull(orderId);
    }

    public void deleteOrder(Integer orderId) {
        orderDao.deleteByIdAndProcessedAtNull(orderId);
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
