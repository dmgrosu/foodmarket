package md.ramaiana.foodmarket.dao;


import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Grosu Kirill (grosukirill009@gmail.com), 2/11/2021
 */


@DataJdbcTest
@Import(DataJdbcConfig.class)
class GoodDaoTest {
    @Autowired
    private GoodDao goodDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private GoodGroupDao goodGroupDao;

    @Test
    void test_create() {
        // ARRANGE
        Brand someBrand = someBrand();
        GoodGroup someGroup = someGroup();
        // ACT
        Good saved = goodDao.save(Good.builder()
                .name("WaterOm")
                .price(15f)
                .brandId(someBrand.getId())
                .groupId(someGroup.getId())
                .unit("")
                .inPackage(5f)
                .erpCode("waterom")
                .barCode("12345")
                .weight(500f)
                .createdAt(OffsetDateTime.now())
                .build());
        // ASSERT
        assertThat(goodDao.existsById(saved.getId())).isTrue();
    }

    @Test
    void test_read() {
        // ARRANGE
        Good someExistingGood = someExistingGood();
        // ACT
        boolean exists = goodDao.existsById(someExistingGood.getId());
        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void test_update() {
        // ARRANGE
        Good someExistingGood = someExistingGood();
        // ACT
        someExistingGood.setPrice(20f);
        Good saved = goodDao.save(someExistingGood);
        // ASSERT
        assertThat(saved.getPrice().equals(20f)).isTrue();
    }

    @Test
    void test_delete() {
        // ARRANGE
        Good someExistingGood = someExistingGood();
        // ACT
        goodDao.deleteById(someExistingGood.getId());
        // ASSERT
        assertThat(goodDao.existsById(someExistingGood.getId())).isFalse();
    }

    private Brand someBrand() {
        return brandDao.save(Brand.builder()
                .name("OM")
                .erpCode("qwerty")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private GoodGroup someGroup() {
        return goodGroupDao.save(GoodGroup.builder()
                .name("Liquids")
                .parentGroupId(1)
                .erpCode("ytrewq")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Good someExistingGood() {
        Brand someBrand = someBrand();
        GoodGroup someGroup = someGroup();
        return goodDao.save(Good.builder()
                .name("WaterOm")
                .price(15f)
                .brandId(someBrand.getId())
                .groupId(someGroup.getId())
                .unit("")
                .inPackage(5f)
                .erpCode("waterom")
                .barCode("12345")
                .weight(500f)
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
