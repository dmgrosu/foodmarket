package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.model.GoodsReadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 3/7/21
 */

@ExtendWith(SpringExtension.class)
class DbfServiceTest {

    @InjectMocks
    private DbfService dbfService;

    @Test
    void test_readGoodsFromFile() throws Exception {
        // ARRANGE
        String filePath = "src/test/resources/testData.dbf";
        // ACT
        GoodsReadResult actualResult = dbfService.readGoodsFromFile(filePath);
        // ASSERT
        assertThat(actualResult.getGoods()).containsKeys("00014170", "00014172", "00021303")
                .doesNotContainKeys("00000005", "00000002");
        assertThat(actualResult.getGoods().values()).extracting("name")
                .contains("ПЕЧЕНЬЕ/Нефис/Фасованые/Роно-крем 45-55гр/Роно крем кокос 45гр 1/12",
                        "СУШКА/Франзелуца/pentru amatori/1/6кг",
                        "МАКАРОНЫ/BUNETTO/1кг 1/10/трубка больш,",
                        "МИВИНА/приправ/курин80гр 1/60");
        assertThat(actualResult.getBrands()).containsKeys("0000009", "R03-005", "BUH-013");
        assertThat(actualResult.getGroups()).containsKeys("00000005", "00000002");
        assertThat(actualResult.getGroups().values()).extracting("name")
                .contains("АНАНАС", "Alfa-Nistru", "Руна/Рiдний край/");
        assertThat(actualResult.getErpCodes()).containsKeys("00014170", "00014172", "00000005");
    }
}
