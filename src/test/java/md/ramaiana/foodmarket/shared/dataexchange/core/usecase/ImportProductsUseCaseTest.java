package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.product.core.usecase.ProductLoadUseCase;
import md.ramaiana.foodmarket.domain.storage.core.usecase.StorageSearchUseCase;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.dataexchange.core.data.ProductReadResult;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.*;

@Tag("unit")
class ImportProductsUseCaseTest {

    private static final String EXCHANGE_FOLDER = "src/test/resources/dataExchange";

    ImportProductsUseCase useCase;
    ProductLoadUseCase productLoad;
    StorageSearchUseCase storageSearch;
    Path path = Paths.get(EXCHANGE_FOLDER, "products-data.xml");

    @BeforeEach
    void setUp() throws Exception {
        productLoad = mock(ProductLoadUseCase.class);
        storageSearch = mock(StorageSearchUseCase.class);
        useCase = new ImportProductsUseCase(productLoad, storageSearch, new DataExchangeConfig().unmarshaller());
        // Deliberately without a trailing separator - the importer must not depend on one.
        useCase.setExchangeFolderPath(EXCHANGE_FOLDER);

        Files.createDirectories(path.getParent());
        String xml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <catalog>
                        <groups>
                            <group code="1c111" name="Конфеты" parentCode=""/>
                            <group code="1c222" name="Букурия" parentCode="1с111"/>
                            <group code="1c333" name="Рошен" parentCode="1с111"/>
                            <group code="1c444" name="Шоколадные" parentCode="1с222"/>
                        </groups>
                        <brands>
                            <brand code="1c10" name="Bucuria"/>
                            <brand code="1c20" name="Roshen"/>
                        </brands>
                        <products>
                            <product code="1с555" name="Метеорит 450гр" brandCode="1c10" groupCode="1с444" weight="0.45" unit="шт" packSize="10">
                                <codes>
                                    <code name="barCode" value="4215355134213"/>
                                    <code name="kauflandCode" value="123456789"/>
                                    <!-- any other code may come here -->
                                </codes>
                                <prices>
                                   <price storageCode="1" type="LOCAL" price="85.13"/>
                                   <price storageCode="1" type="RETAIL_ZONE1" price="88.53"/>
                                   <price storageCode="2" type="LOCAL" price="84.49"/>
                                   <price storageCode="4" type="LOCAL" price="84.57"/>
                               </prices>
                            </product>
                            <product code="1c666" name="Pasarea Maiastra 300gr" brandCode="1c10" groupCode="1c444" weight="0.3" unit="buc" packSize="12">
                                <codes>
                                    <code name="barCode" value="4785468724687"/>
                                </codes>
                                <prices>
                                    <price storageCode="1" type="LOCAL" price="57.54"/>
                                    <price storageCode="2" type="LOCAL" price="53.16"/>
                                </prices>
                            </product>
                        </products>
                    </catalog>""";
        Files.writeString(path, xml);
    }

    @Test
    void should_import_products() {
        when(storageSearch.findByErpCode("1")).thenReturn(new StorageEntity(1, "storage 1", "1"));
        when(storageSearch.findByErpCode("2")).thenReturn(new StorageEntity(2, "storage 2", "2"));
        when(storageSearch.findByErpCode("4")).thenReturn(new StorageEntity(4, "storage 4", "4"));

        useCase.execute();

        ArgumentCaptor<ProductReadResult> captor = ArgumentCaptor.forClass(ProductReadResult.class);
        verify(productLoad).execute(captor.capture());
        ProductReadResult readResult = captor.getValue();
        assertThat(readResult.getProducts())
                .containsOnlyKeys("1с555", "1c666");
        assertThat(readResult.getProducts().get("1с555"))
                .extracting("name", "unit", "inPackage", "barCode", "weight")
                .containsExactly("Метеорит 450гр", "шт", 10f, "4215355134213", 0.45f);
        assertThat(readResult.getProducts().get("1с555").getPrices())
                .extracting("storage", "type", "price")
                .containsExactlyInAnyOrder(
                        tuple(AggregateReference.to(1), PriceType.LOCAL, 85.13f),
                        tuple(AggregateReference.to(1), PriceType.RETAIL_ZONE1, 88.53f),
                        tuple(AggregateReference.to(2), PriceType.LOCAL, 84.49f),
                        tuple(AggregateReference.to(4), PriceType.LOCAL, 84.57f)
                );
        assertThat(readResult.getGroups())
                .containsOnlyKeys("1c111", "1c222", "1c333", "1c444");
        assertThat(readResult.getBrands())
                .containsOnlyKeys("1c10", "1c20");
        assertThat(readResult.getErpCodes())
                .containsOnlyKeys("1c111", "1c222", "1c333", "1c444", "1с555", "1c666");
    }

}