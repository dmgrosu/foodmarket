package md.ramaiana.foodmarket.dao;


import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJdbcTest
@Import(DataJdbcConfig.class)
class ProductDaoTest {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private ProductGroupDao productGroupDao;

    @Test
    void test_create() {
        // ARRANGE
        Brand someBrand = someBrand();
        ProductGroup someGroup = someGroup();
        // ACT
        Product saved = productDao.save(Product.builder()
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
        assertThat(productDao.existsById(saved.getId())).isTrue();
    }

    @Test
    void test_read() {
        // ARRANGE
        Product someExistingProduct = someExistingProduct();
        // ACT
        boolean exists = productDao.existsById(someExistingProduct.getId());
        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void test_update() {
        // ARRANGE
        Product existingProduct = someExistingProduct();
        Product updated = new Product(
                existingProduct.getId(),
                "new name",
                20f,
                existingProduct.getUnit(),
                existingProduct.getInPackage(),
                existingProduct.getErpCode(),
                existingProduct.getBarCode(),
                1.35f,
                existingProduct.getBrandId(),
                existingProduct.getGroupId(),
                existingProduct.getCreatedAt(),
                existingProduct.getDeletedAt(),
                OffsetDateTime.now()
        );
        // ACT
        Product saved = productDao.save(updated);
        // ASSERT
        assertThat(saved.getPrice()).isEqualTo(20f);
        assertThat(saved.getWeight()).isEqualTo(1.35f);
        assertThat(saved.getName()).isEqualTo("new name");
    }

    @Test
    void test_delete() {
        // ARRANGE
        Product someExistingProduct = someExistingProduct();
        // ACT
        productDao.deleteById(someExistingProduct.getId());
        // ASSERT
        assertThat(productDao.existsById(someExistingProduct.getId())).isFalse();
    }

    @Test
    void test_saveGood_saved() {
        // ARRANGE
        Product givenProduct = Product.builder()
                .name("someName")
                .erpCode("1234455")
                .price(123.55f)
                .createdAt(OffsetDateTime.now())
                .build();
        // ACT
        Product actualProduct = productDao.save(givenProduct);
        // ASSERT
        Assertions.assertThat(actualProduct.getId()).isNotNull();
    }

    private Brand someBrand() {
        return brandDao.save(Brand.builder()
                .name("OM")
                .erpCode("qwerty")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private ProductGroup someGroup() {
        return productGroupDao.save(ProductGroup.builder()
                .name("Liquids")
                .parentGroupId(1)
                .erpCode("ytrewq")
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Product someExistingProduct() {
        Brand someBrand = someBrand();
        ProductGroup someGroup = someGroup();
        return productDao.save(Product.builder()
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
