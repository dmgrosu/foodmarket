package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.GoodGroupDao;
import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
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
class GoodServiceTest {
    @Mock
    private GoodDao goodDaoMock;

    @Mock
    private GoodGroupDao goodGroupDaoMock;

    @InjectMocks
    private GoodService goodService;

    @Test
    void test_findGoodsFiltered_allParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
        String goodName = "someGoodName";
        List<Good> goods = givenGoods(groupId, brandId, goodName);
        when(goodDaoMock.getAllByGroupIdAndBrandIdAndNameLikeAndDeletedAtNull(eq(groupId), eq(brandId), eq(goodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, brandId, goodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndBrandIdAndNameLikeAndDeletedAtNull(groupId, brandId, goodName);
    }

    @Test
    void test_findGoodsFiltered_groupIdAndBrandIdParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
        List<Good> goods = givenGoods(groupId, brandId, null);
        when(goodDaoMock.getAllByGroupIdAndBrandIdAndDeletedAtNull(eq(groupId), eq(brandId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, brandId, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndBrandIdAndDeletedAtNull(groupId, brandId);
    }


    @Test
    void test_findGoodsFiltered_brandIdAndNameParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        String someGoodName = "someGoodName";
        List<Good> goods = givenGoods(null, brandId, someGoodName);
        when(goodDaoMock.getAllByBrandIdAndNameAndDeletedAtNull(eq(brandId), eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, brandId, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByBrandIdAndNameAndDeletedAtNull(brandId, someGoodName);
    }

    @Test
    void test_findGoodsFiltered_groupIdAndNameParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
        String someGoodName = "someGoodName";
        List<Good> goods = givenGoods(groupId, null, someGoodName);
        when(goodDaoMock.getAllByGroupIdAndNameAndDeletedAtNull(eq(groupId), eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, null, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndNameAndDeletedAtNull(groupId, someGoodName);
    }

    @Test
    void test_findGoodsFiltered_withOnlyGroupIdParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
        List<Good> goods = givenGoods(groupId, null, null);
        when(goodDaoMock.getAllByGroupIdAndDeletedAtNull(eq(groupId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, null, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndDeletedAtNull(groupId);
    }

    @Test
    void test_findGoodsFiltered_withOnlyBrandIdParam_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        List<Good> goods = givenGoods(null, brandId, null);
        when(goodDaoMock.getAllByBrandIdAndDeletedAtNull(eq(brandId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, brandId, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByBrandIdAndDeletedAtNull(brandId);
    }

    @Test
    void test_findGoodsFiltered_withOnlyNameParam_returnedList() {
        //ARRANGE
        String someGoodName = "someName";
        List<Good> goods = givenGoods(null, null, someGoodName);
        when(goodDaoMock.getAllByNameAndDeletedAtNull(eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, null, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByNameAndDeletedAtNull(someGoodName);
    }

    @Test
    void test_findGoodsFiltered_withNoParams_returnedList() {
        //ARRANGE
        List<Good> goods = givenGoods(null, null, null);
        when(goodDaoMock.getAllByGroupIdNullAndDeletedAtNull())
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, null, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdNullAndDeletedAtNull();
    }

    @Test
    void test_findGroupsFiltered_withParentGroupIdParam_returnedList() {
        //ARRANGE
        List<GoodGroup> groups = givenGroups(5);
        when(goodGroupDaoMock.getAllByParentGroupIdAndDeletedAtNullOrderByName(5))
                .thenReturn(groups);
        //ACT
        List<GoodGroup> returnedGroups = goodService.getGroupsHierarchy(5);
        //ASSERT
        verify(goodGroupDaoMock, times(1))
                .getAllByParentGroupIdAndDeletedAtNullOrderByName(5);

        assertThat(returnedGroups.get(0).getId()).isEqualTo(1);
    }



    @Test
    void test_findGroupsFiltered_withNoParams_returnedList() {
        //ARRANGE
        List<GoodGroup> groups = givenGroups(null);
        when(goodGroupDaoMock.getAllByParentGroupIdNullAndDeletedAtNullOrderByName())
                .thenReturn(groups);
        //ACT
        List<GoodGroup> returnedGroups = goodService.getGroupsHierarchy(null);
        //ASSERT
        verify(goodGroupDaoMock, times(1))
                .getAllByParentGroupIdNullAndDeletedAtNullOrderByName();

        assertThat(returnedGroups.get(0).getId()).isEqualTo(5);
    }

    private List<Good> givenGoods(Integer groupId, Integer brandId, String name) {
        if (groupId != null && brandId != null && name != null){
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name(name)
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (groupId != null && brandId != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (groupId != null && name != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name(name)
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (brandId != null && name != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name(name)
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (groupId != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(groupId)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (brandId != null) {
            Brand someBrand = Brand.builder()
                    .id(brandId)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else if (name != null) {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        } else {
            Brand someBrand = Brand.builder()
                    .id(2)
                    .name("someName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(1)
                    .name("someName")
                    .build();
            List<Good> goods = new ArrayList<>();
            goods.add(Good.builder()
                    .id(3)
                    .name("someName")
                    .brandId(someBrand.getId())
                    .groupId(someGroup.getId())
                    .price(15f)
                    .build());
            return goods;
        }
    }

    private List<GoodGroup> givenGroups(Integer groupId) {
        if (groupId != null) {
            GoodGroup parentGroup = GoodGroup.builder()
                    .id(groupId)
                    .name("someParentGroupName")
                    .build();
            GoodGroup someGroup = GoodGroup.builder()
                    .id(1)
                    .name("someName")
                    .parentGroupId(parentGroup.getId())
                    .build();
            List<GoodGroup> groups = new ArrayList<>();
            groups.add(someGroup);
            return groups;
        } else {
            GoodGroup parentGroup = GoodGroup.builder()
                    .id(5)
                    .name("someParentGroupName")
                    .build();
            List<GoodGroup> groups = new ArrayList<>();
            groups.add(parentGroup);
            return groups;
        }
    }
}
