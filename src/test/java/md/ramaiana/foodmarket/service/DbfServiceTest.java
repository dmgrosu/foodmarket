package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.model.GoodsReadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class DbfServiceTest {

    @InjectMocks
    private DbfService dbfService;

    @Test
    void test_readGoodsFromFile() {
        // ARRANGE
        String filePath = "src/test/resources/testData.dbf";
        // ACT
        GoodsReadResult actualResult = dbfService.readGoodsFromFile(filePath);
        // ASSERT
        assertThat(actualResult.getGoods()).extracting("erpCode")
                .contains("00014170", "00014172", "00021303")
                .doesNotContain("00000005", "00000002");
        assertThat(actualResult.getGoods()).extracting("name")
                .contains("ПЕЧЕНЬЕ/Нефис/Фасованые/Роно-крем 45-55гр/Роно крем кокос 45гр 1/12",
                        "СУШКА/Франзелуца/pentru amatori/1/6кг",
                        "МАКАРОНЫ/BUNETTO/1кг 1/10/трубка больш,",
                        "МИВИНА/приправ/курин80гр 1/60");
        assertThat(actualResult.getBrands()).extracting("erpCode")
                .contains("0000009", "R03-005", "BUH-013");
        assertThat(actualResult.getGroups()).extracting("erpCode")
                .contains("00000005", "00000002");
        assertThat(actualResult.getGroups()).extracting("name")
                .contains("АНАНАС", "Alfa-Nistru", "Руна/Рiдний край/");
    }
}
