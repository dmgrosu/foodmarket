package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.price.core.usecase.PricesUpdateUseCase;
import md.ramaiana.foodmarket.domain.product.core.usecase.BalancesUpdateUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpBalanceDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpPriceDto;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application.yml")
class ImportPricesAndBalancesUseCaseTest {
    @TestConfiguration
    static class TextConfig {
        @Value("${dataFolderPath}")
        private String folderPath;

        @Bean
        public ImportPricesAndBalancesUseCase useCase() {
            DataExchangeConfig config = new DataExchangeConfig();
            ImportPricesAndBalancesUseCase useCase = new ImportPricesAndBalancesUseCase(pricesUpdateUseCase(), balancesUpdateUseCase(), config.unmarshaller());
            useCase.setExchangeFolderPath(folderPath);
            return useCase;
        }

        @Bean
        public PricesUpdateUseCase pricesUpdateUseCase() {
            return mock(PricesUpdateUseCase.class);
        }

        @Bean
        public BalancesUpdateUseCase balancesUpdateUseCase() {
            return mock(BalancesUpdateUseCase.class);
        }
    }

    @Autowired
    ImportPricesAndBalancesUseCase useCase;
    @Autowired
    BalancesUpdateUseCase balancesUpdate;
    @Autowired
    PricesUpdateUseCase pricesUpdate;
    @Captor
    ArgumentCaptor<List<ErpBalanceDto>> balancesCaptor;
    @Captor
    ArgumentCaptor<List<ErpPriceDto>> pricesCaptor;

    @Test
    void should_import_prices_and_balances() {
        useCase.execute();

        verify(pricesUpdate).execute(pricesCaptor.capture());
        verify(balancesUpdate).execute(balancesCaptor.capture());
        List<ErpPriceDto> actualPrices =  pricesCaptor.getValue();
        assertThat(actualPrices)
                .isNotNull()
                .extracting("storageCode", "productCode", "type", "price")
                .containsExactly(
                        tuple("1", "1c555", PriceType.LOCAL, 85.13f),
                        tuple("1", "1c555", PriceType.RETAIL_ZONE1, 88.53f),
                        tuple("1", "1c666", PriceType.LOCAL, 57.54f),
                        tuple("2", "1c555", PriceType.LOCAL, 84.49f),
                        tuple("4", "1c555", PriceType.LOCAL, 84.57f)
                );
        List<ErpBalanceDto> actualBalances = balancesCaptor.getValue();
        assertThat(actualBalances)
                .isNotNull()
                .extracting("storageCode", "productCode", "quantity")
                .containsExactly(
                        tuple("1", "1c555", 412f),
                        tuple("1", "1c666", 46f),
                        tuple("2", "1c555", 38f),
                        tuple("3", "1c555", 76f),
                        tuple("4", "1c555", 188f)
                );
    }

}