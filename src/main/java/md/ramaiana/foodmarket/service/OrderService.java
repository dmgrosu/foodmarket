package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Grosu Kirill (grosukirill009@gmail.com), 2/12/2021
 **/

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final ClientDao clientDao;

    @Autowired
    public OrderService(OrderDao orderDao,
                        ClientDao clientDao) {
        this.clientDao = clientDao;
        this.orderDao = orderDao;
    }
}
