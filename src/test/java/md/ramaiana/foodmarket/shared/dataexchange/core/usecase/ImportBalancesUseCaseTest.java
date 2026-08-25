package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.product.core.usecase.BalancesUpdateUseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpBalanceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("integration")
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application.yml")
class ImportBalancesUseCaseTest {
    @TestConfiguration
    static class TextConfig {
        @Value("${dataFolderPath}")
        private String folderPath;

        @Bean
        public ImportBalancesUseCase useCase() {
            DataExchangeConfig config = new DataExchangeConfig();
            ImportBalancesUseCase useCase = new ImportBalancesUseCase(balancesUpdateUseCase(), config.unmarshaller());
            useCase.setExchangeFolderPath(folderPath);
            return useCase;
        }
        @Bean
        public BalancesUpdateUseCase balancesUpdateUseCase() {
            return mock(BalancesUpdateUseCase.class);
        }
    }

    @Autowired
    ImportBalancesUseCase useCase;
    @Autowired
    BalancesUpdateUseCase balancesUpdate;
    @Captor
    ArgumentCaptor<List<ErpBalanceDto>> balancesCaptor;
    Path path = Paths.get("src/test/resources/dataExchange/balances-data.xml");

    @BeforeEach
    void setUp() throws Exception {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            String xml = """
                    <?xml version="1.0" encoding="Windows-1251"?>
                    <balance-data>
                       <balances>
                           <balance storageCode="1" productCode="1c555" quantity="412"/>
                           <balance storageCode="1" productCode="1c666" quantity="46"/>
                           <balance storageCode="2" productCode="1c555" quantity="38"/>
                           <balance storageCode="3" productCode="1c555" quantity="76"/>
                           <balance storageCode="4" productCode="1c555" quantity="188"/>
                       </balances>
                    </balance-data>""";
            Files.writeString(path, xml);
        }
    }

    @Test
    void should_import_balances() {
        useCase.execute();

        verify(balancesUpdate).execute(balancesCaptor.capture());
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