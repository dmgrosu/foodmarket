package md.ramaiana.foodmarket.service.dataexchange;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.model.ProductReadResult;
import md.ramaiana.foodmarket.service.ProductService;
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
@TestPropertySource(locations = "classpath:application.properties")
class XmlDataExchangeServiceTest {

    @TestConfiguration
    static class TextConfig {
        @Value("${dataFilePath}")
        private String filePath;
        @Bean
        public XmlDataExchangeService xmlDataExchangeService() {
            DataExchangeConfig config = new DataExchangeConfig();
            XmlDataExchangeService service = new XmlDataExchangeService(productService(), config.marshaller(), config.unmarshaller());
            service.setFilePath(filePath);
            return service;
        }
        @Bean
        public ProductService productService() {
            return mock(ProductService.class);
        }
    }

    @Autowired
    XmlDataExchangeService service;
    @Autowired
    ProductService productService;

    @Test
    void should_import_products() throws Exception {
        service.importProducts();

        ArgumentCaptor<ProductReadResult> captor = ArgumentCaptor.forClass(ProductReadResult.class);
        verify(productService).loadProducts(captor.capture());
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