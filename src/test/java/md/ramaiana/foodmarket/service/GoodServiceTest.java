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
    void test_getAllGoods() {
        //ARRANGE
        //ACT
        goodService.getAllGoods();
        //ASSERT
        verify(goodDaoMock, times(1)).getAllByDeletedAtNull();
    }

    @Test
    void test_findGoodsFiltered_allParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
        String goodName = "someGoodName";
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
                .name(goodName)
                .brandId(someBrand.getId())
                .groupId(someGroup.getId())
                .price(15f)
                .build());
        when(goodDaoMock.getAllByGroupIdAndBrandIdAndNameLike(eq(groupId), eq(brandId), eq(goodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, brandId, goodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndBrandIdAndNameLike(groupId, brandId, goodName);
    }

    @Test
    void test_findGoodsFiltered_groupIdAndBrandIdParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        Integer groupId = 1;
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
        when(goodDaoMock.getAllByGroupIdAndBrandId(eq(groupId), eq(brandId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, brandId, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndBrandId(groupId, brandId);
    }


    @Test
    void test_findGoodsFiltered_brandIdAndNameParams_returnedList() {
        //ARRANGE
        Integer brandId = 2;
        String someGoodName = "someGoodName";
        Brand someBrand = Brand.builder()
                .id(brandId)
                .name(someGoodName)
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
        when(goodDaoMock.getAllByBrandIdAndName(eq(brandId), eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, brandId, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByBrandIdAndName(brandId, someGoodName);
    }

    @Test
    void test_findGoodsFiltered_groupIdAndNameParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
        String someGoodName = "someGoodName";
        Brand someBrand = Brand.builder()
                .id(2)
                .name(someGoodName)
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
        when(goodDaoMock.getAllByGroupIdAndName(eq(groupId), eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, null, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupIdAndName(groupId, someGoodName);
    }

    @Test
    void test_findGoodsFiltered_withOnlyGroupIdParam_returnedList() {
        //ARRANGE
        Integer groupId = 1;
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
        when(goodDaoMock.getAllByGroupId(eq(groupId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(groupId, null, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByGroupId(groupId);
    }

    @Test
    void test_findGoodsFiltered_withOnlyBrandIdParam_returnedList() {
        //ARRANGE
        Integer brandId = 2;
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
        when(goodDaoMock.getAllByBrandId(eq(brandId)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, brandId, null);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByBrandId(brandId);
    }

    @Test
    void test_findGoodsFiltered_withOnlyNameParam_returnedList() {
        //ARRANGE
        String someGoodName = "someName";
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
        when(goodDaoMock.getAllByName(eq(someGoodName)))
                .thenReturn(goods);
        //ACT
        goodService.findGoodsFiltered(null, null, someGoodName);
        //ASSERT
        verify(goodDaoMock, times(1))
                .getAllByName(someGoodName);
    }

    @Test
    void test_findGoodsFiltered_withNoParams_returnedList() {
        //ARRANGE
        String someGoodName = "someName";
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
        GoodGroup parentGroup = GoodGroup.builder()
                .id(5)
                .name("someParentGroupName")
                .build();
        GoodGroup someGroup = GoodGroup.builder()
                .id(1)
                .name("someName")
                .parentGroupId(parentGroup.getId())
                .build();
        List<GoodGroup> groups = new ArrayList<>();
        groups.add(someGroup);
        when(goodGroupDaoMock.getAllByParentGroupIdAndDeletedAtNull(5))
                .thenReturn(groups);
        //ACT
        List<GoodGroup> returnedGroups = goodService.findGroupsFiltered(5);
        //ASSERT
        verify(goodGroupDaoMock, times(1))
                .getAllByParentGroupIdAndDeletedAtNull(5);

        assertThat(returnedGroups.get(0).getId()).isEqualTo(1);
    }

    @Test
    void test_findGroupsFiltered_withNoParams_returnedList() {
        //ARRANGE
        GoodGroup parentGroup = GoodGroup.builder()
                .id(5)
                .name("someParentGroupName")
                .build();
        List<GoodGroup> groups = new ArrayList<>();
        groups.add(parentGroup);
        when(goodGroupDaoMock.getAllByParentGroupIdNullAndDeletedAtNull())
                .thenReturn(groups);
        //ACT
        List<GoodGroup> returnedGroups = goodService.findGroupsFiltered(null);
        //ASSERT
        verify(goodGroupDaoMock, times(1))
                .getAllByParentGroupIdNullAndDeletedAtNull();

        assertThat(returnedGroups.get(0).getId()).isEqualTo(5);
    }
}
