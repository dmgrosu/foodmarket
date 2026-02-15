package md.ramaiana.foodmarket.service.dataexchange;

import java.io.IOException;

public interface DataExchangeService {

    void importProducts() throws IOException;

    void exportOrders() throws IOException;

}
