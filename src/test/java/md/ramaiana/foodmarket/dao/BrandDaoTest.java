package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Brand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataJdbcConfig.class)
class BrandDaoTest {

    @Autowired
    private BrandDao brandDao;

    @Test
    void test_saveNewBrand_saved() {
        // ARRANGE
        Brand givenBrand = Brand.builder()
                .name("someName")
                .erpCode("123456")
                .createdAt(OffsetDateTime.now())
                .build();
        // ACT
        Brand actualBrand = brandDao.save(givenBrand);
        // ASSERT
        assertThat(actualBrand.getId()).isNotNull();
    }

    @Test
    void test_getById_foundBrandReturned() {
        // ARRANGE
        Brand someBrand = someExistingBrand("someName", "erpCode");
        // ACT
        Brand actualBrand = brandDao.findByIdAndDeletedAtIsNull(someBrand.getId()).get();
        // ASSERT
        assertThat(actualBrand.getId()).isNotNull();
        assertThat(actualBrand.getErpCode()).isEqualTo("erpCode");
        assertThat(actualBrand.getName()).isEqualTo("someName");
    }

    private Brand someExistingBrand(String name, String erpCode) {
        Brand brand = Brand.builder()
                .name(name)
                .erpCode(erpCode)
                .createdAt(OffsetDateTime.now())
                .build();
        return brandDao.save(brand);
    }

}
