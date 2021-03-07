package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.OrderDao;
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

    @Autowired
    public OrderService(OrderDao orderDao,
                        GoodDao goodDao) {
        this.orderDao = orderDao;
        this.goodDao = goodDao;
    }

    public Order findOrdersById(Integer orderId) {
        return orderDao.getByIdAndDeletedAtNull(orderId);
    }

    public void deleteOrder(Integer orderId) {
        orderDao.deleteByIdAndProcessedAtNull(orderId);
    }

    public Order addGoodToOrder(Integer orderId, Integer goodId, Float quantity, Integer clientId) {
        Float sum = goodDao.getById(goodId).getPrice() * quantity;
        return orderDao.save(Order.builder()
                .id(orderId)
                .clientId(clientId)
                .totalSum(sum)
                .build());
    }
}
