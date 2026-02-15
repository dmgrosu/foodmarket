package md.ramaiana.foodmarket.service.dataexchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleDataService {

    private final DataExchangeService dataExchangeService;

    @Scheduled(fixedDelayString = "${dataLoadingDelay}")
    public void runDataExchange() {
        try {
            dataExchangeService.exportOrders();
        } catch (IOException e) {
            log.error("Could not export orders: {}", e.getMessage(), e);
        }
        try {
            dataExchangeService.importProducts();
        } catch (IOException e) {
            log.error("Could not import products: {}", e.getMessage(), e);
        }
    }

}
