package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ProductDao;
import md.ramaiana.foodmarket.dao.ProductGroupDao;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.ProductGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {
    @Mock
    private ProductDao productDaoMock;

    @Mock
    private ProductGroupDao productGroupDaoMock;

    @InjectMocks
    private ProductService productService;

    @Test
    void test_findProductsFiltered_allParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
        String goodName = "someGoodName";
        List<Product> products = givenGoods(groupId, brandId, goodName);
        when(productDaoMock.getAllByGroupIdAndBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(eq(groupId), eq(brandId), eq(goodName)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(groupId, brandId, goodName);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByGroupIdAndBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, brandId, goodName);
    }

    @Test
    void test_findProductsFiltered_groupIdAndBrandIdParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
        List<Product> products = givenGoods(groupId, brandId, null);
        when(productDaoMock.getAllByGroupIdAndBrandIdAndDeletedAtNull(eq(groupId), eq(brandId)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(groupId, brandId, null);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId);
    }


    @Test
    void test_findProductsFiltered_brandIdAndNameParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        String someGoodName = "someGoodName";
        List<Product> products = givenGoods(null, brandId, someGoodName);
        when(productDaoMock.getAllByBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(eq(brandId), eq(someGoodName)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(null, brandId, someGoodName);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(brandId, someGoodName);
    }

    @Test
    void test_findProductsFiltered_groupIdAndNameParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
        String someGoodName = "someGoodName";
        List<Product> products = givenGoods(groupId, null, someGoodName);
        when(productDaoMock.getAllByGroupIdAndNameIgnoreCaseContainingAndDeletedAtNull(eq(groupId), eq(someGoodName)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(groupId, null, someGoodName);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByGroupIdAndNameIgnoreCaseContainingAndDeletedAtNull(groupId, someGoodName);
    }

    @Test
    void test_findProductsFiltered_withOnlyGroupIdParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
        List<Product> products = givenGoods(groupId, null, null);
        when(productDaoMock.getAllByGroupIdAndDeletedAtNull(eq(groupId)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(groupId, null, null);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByGroupIdAndDeletedAtNull(groupId);
    }

    @Test
    void test_findProductsFiltered_withOnlyBrandIdParam_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        List<Product> products = givenGoods(null, brandId, null);
        when(productDaoMock.getAllByBrandIdAndDeletedAtNull(eq(brandId)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(null, brandId, null);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByBrandIdAndDeletedAtNull(brandId);
    }

    @Test
    void test_findProductsFiltered_withOnlyNameParam_returnedList() {
        //ARRANGE
        String someGoodName = "someName";
        List<Product> products = givenGoods(null, null, someGoodName);
        when(productDaoMock.getAllByNameIgnoreCaseContainingAndDeletedAtNull(eq(someGoodName)))
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(null, null, someGoodName);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByNameIgnoreCaseContainingAndDeletedAtNull(someGoodName);
    }

    @Test
    void test_findProductsFiltered_withNoParams_returnedList() {
        //ARRANGE
        List<Product> products = givenGoods(null, null, null);
        when(productDaoMock.getAllByGroupIdNullAndDeletedAtNull())
                .thenReturn(products);
        //ACT
        productService.findProductsFiltered(null, null, null);
        //ASSERT
        verify(productDaoMock, times(1))
                .getAllByGroupIdNullAndDeletedAtNull();
    }

    @Test
    void test_findGroupsFiltered_withParentGroupIdParam_returnedList() {
        //ARRANGE
        List<ProductGroup> groups = givenGroups(5);
        when(productGroupDaoMock.getAllByParentGroupIdAndDeletedAtNullOrderByName(5))
                .thenReturn(groups);
        //ACT
        List<ProductGroup> returnedGroups = productService.getGroupsHierarchy(5);
        //ASSERT
        verify(productGroupDaoMock, times(1))
                .getAllByParentGroupIdAndDeletedAtNullOrderByName(5);

        assertThat(returnedGroups.get(0).getId()).isEqualTo(1);
    }



    @Test
    void test_findGroupsFiltered_withNoParams_returnedList() {
        //ARRANGE
        List<ProductGroup> groups = givenGroups(null);
        when(productGroupDaoMock.findByParentGroupIdNullAndDeletedAtNullOrderByName())
                .thenReturn(groups);
        //ACT
        List<ProductGroup> returnedGroups = productService.getGroupsHierarchy(null);
        //ASSERT
        verify(productGroupDaoMock, times(1))
                .findByParentGroupIdNullAndDeletedAtNullOrderByName();

        assertThat(returnedGroups.get(0).getId()).isEqualTo(5);
    }

    private List<Product> givenGoods(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null){
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name(name)
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (groupId != null && brandId != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (groupId != null && name != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name(name)
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (brandId != null && name != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name(name)
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (groupId != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (brandId != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else if (name != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        } else {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Product> products = new ArrayList<>();
            products.add(Product.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return products;
        }
    }

    private List<ProductGroup> givenGroups(Integer groupId) {
        if (groupId != null) {
            ProductGroup parentGroup = ProductGroup.builder()
                    .id(groupId)
                    .name("someParentGroupName")
                    .build();
            ProductGroup someGroup = ProductGroup.builder()
                    .id(1)
                    .name("someName")
                    .parentGroupId(parentGroup.getId())
                    .build();
            List<ProductGroup> groups = new ArrayList<>();
            groups.add(someGroup);
            return groups;
        } else {
            ProductGroup parentGroup = ProductGroup.builder()
                    .id(5)
                    .name("someParentGroupName")
                    .build();
            List<ProductGroup> groups = new ArrayList<>();
            groups.add(parentGroup);
            return groups;
        }
    }
}
