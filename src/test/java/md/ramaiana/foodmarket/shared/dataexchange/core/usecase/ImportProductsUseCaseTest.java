package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductLoadUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.core.data.ProductReadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application.yml")
class ImportProductsUseCaseTest {

    @TestConfiguration
    static class TextConfig {
        @Value("${dataFolderPath}")
        private String folderPath;
        @Bean
        public ImportProductsUseCase useCase() {
            DataExchangeConfig config = new DataExchangeConfig();
            ImportProductsUseCase useCase = new ImportProductsUseCase(productLoadUseCase(), config.unmarshaller());
            useCase.setExchangeFolderPath(folderPath);
            return useCase;
        }
        @Bean
        public ProductLoadUseCase productLoadUseCase() {
            return mock(ProductLoadUseCase.class);
        }
    }

    @Autowired
    ImportProductsUseCase useCase;
    @Autowired
    ProductLoadUseCase productLoad;

    @Test
    void should_import_products() {
        useCase.execute();

        ArgumentCaptor<ProductReadResult> captor = ArgumentCaptor.forClass(ProductReadResult.class);
        verify(productLoad).execute(captor.capture());
        ProductReadResult readResult = captor.getValue();
        assertThat(readResult.getProducts())
                .containsOnlyKeys("1с555", "1c666");
        assertThat(readResult.getProducts().get("1с555"))
                .extracting("name", "unit", "inPackage", "barCode", "weight")
                .containsExactly("Метеорит 450гр", "шт", 10f, "4215355134213", 0.45f);
        assertThat(readResult.getGroups())
                .containsOnlyKeys("1c111", "1c222", "1c333", "1c444");
        assertThat(readResult.getBrands())
                .containsOnlyKeys("1c10", "1c20");
        assertThat(readResult.getErpCodes())
                .containsOnlyKeys("1c111", "1c222", "1c333", "1c444", "1с555", "1c666");
    }

}