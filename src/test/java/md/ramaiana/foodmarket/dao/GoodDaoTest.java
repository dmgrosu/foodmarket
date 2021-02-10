package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Good;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
class GoodDaoTest {

    @Autowired
    private GoodDao goodDao;

    @Test
    void test_saveGood_saved() {
        // ARRANGE
        Good givenGood = Good.builder()
                .name("someName")
                .erpCode("1234455")
                .price(123.55f)
                .createdAt(OffsetDateTime.now())
                .build();
        // ACT
        Good actualGood = goodDao.save(givenGood);
        // ASSERT
        assertThat(actualGood.getId()).isNotNull();
    }
}
