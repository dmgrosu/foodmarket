package md.ramaiana.foodmarket.shared.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportClientsUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportPricesAndBalancesUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.usecase.ImportProductsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleDataService {

    private final ImportProductsUseCase importProducts;
    private final ImportPricesAndBalancesUseCase importPricesAndBalances;
    private final ImportClientsUseCase importClients;

    @Scheduled(fixedDelayString = "${dataLoadingDelay}")
    public void runDataExchange() {
        log.info("Running data exchange...");
        importProducts();
        importPricesAndBalances();
        importClients();
        exportOrders();
    }

    private void importClients() {
        try {
            importClients.execute();
        } catch (Exception e) {
            log.error("Could not import prices and balances: {}", e.getMessage(), e);
        }
    }

    private void importPricesAndBalances() {
        try {
            importPricesAndBalances.execute();
        } catch (Exception e) {
            log.error("Could not import prices and balances: {}", e.getMessage(), e);
        }
    }

    private void importProducts() {
        try {
            importProducts.execute();
        } catch (Exception e) {
            log.error("Could not import products: {}", e.getMessage(), e);
        }
    }

    private void exportOrders() {
        // TODO
    }

}
