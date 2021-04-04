package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.service.GoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class GoodControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodService goodServiceMock;

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withGroupId() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithGroupId(2);
        List<GoodGroup> givenGroups = givenGroupsWithId(2);
        when(goodServiceMock.findGoodsFiltered(eq(8), eq(null), eq(null)))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(8)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", "8")
                .param("brand_id", (String) null)
                .param("name", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("SomeGood"))
                .andExpect(jsonPath("$.goods[1].name").value("SomeOtherGood"))
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withGroupIdAndBrandId() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithGroupIdAndBrandId(8, 5);
        List<GoodGroup> givenGroups = givenGroupsWithId(8);
        when(goodServiceMock.findGoodsFiltered(eq(8), eq(5), eq(null)))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(8)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", "8")
                .param("brand_id", "5")
                .param("name", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("SomeGood"))
                .andExpect(jsonPath("$.goods[1].name").value("SomeOtherGood"))
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withGroupIdAndBrandIdAndName() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithGroupIdAndBrandIdAndName(8, 5, "someName");
        List<GoodGroup> givenGroups = givenGroupsWithId(8);
        when(goodServiceMock.findGoodsFiltered(eq(8), eq(5), eq("someName")))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(8)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", "8")
                .param("brand_id", "5")
                .param("name", "someName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[1].name").value("someName other"))
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withGroupIdAndName() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithGroupIdAndName(5, "someName");
        List<GoodGroup> givenGroups = givenGroupsWithId(2);
        when(goodServiceMock.findGoodsFiltered(eq(5), eq(null), eq("someName")))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(5)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", "5")
                .param("brand_id", (String) null)
                .param("name", "someName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[1].name").value("someName other"))
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].id").value(16));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withBrandId() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithBrandId(5);
        List<GoodGroup> givenGroups = givenGroupsWithId(null);
        when(goodServiceMock.findGoodsFiltered(eq(null), eq(5), eq(null)))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(null)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", (String) null)
                .param("brand_id", "5")
                .param("name", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[1].name").value("otherName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withName() throws Exception {
        //ARRANGE
        List<Good> givenGoods = givenGoodsWithName("someName");
        List<GoodGroup> givenGroups = givenGroupsWithId(null);
        when(goodServiceMock.findGoodsFiltered(eq(null), eq(null), eq("someName")))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(null)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", (String) null)
                .param("brand_id", (String) null)
                .param("name", "someName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[1].name").value("someName other"));
    }

    @WithMockUser("spring")
    @Test
    void test_getFilteredGoods_withBrandIdAndName() throws Exception {
        List<Good> givenGoods = givenGoodsWithBrandIdAndName(5, "someName");
        List<GoodGroup> givenGroups = givenGroupsWithId(null);
        when(goodServiceMock.findGoodsFiltered(eq(null), eq(5), eq("someName")))
                .thenReturn(givenGoods);
        when(goodServiceMock.findGroupsFiltered(eq(null)))
                .thenReturn(givenGroups);
        //ACT & ASSERT
        mockMvc.perform(get("/good/getAll")
                .param("group_id", (String) null)
                .param("brand_id", "5")
                .param("name", "someName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[1].name").value("someName other"))
                .andExpect(jsonPath("$.goods[0].weight").value(1000f));
    }

    private List<GoodGroup> givenGroupsWithId(Integer parentGroupId) {

        List<GoodGroup> groups = new ArrayList<>();
        if (parentGroupId == null) return groups;
        groups.add(GoodGroup.builder()
                .id(15)
                .name("someGroupName")
                .parentGroupId(parentGroupId)
                .build());
        groups.add(GoodGroup.builder()
                .id(16)
                .name("someOtherGroupName")
                .parentGroupId(parentGroupId)
                .build());
        return groups;
    }

    private List<Good> givenGoodsWithGroupId(Integer groupId) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name("SomeGood")
                .price(15f)
                .brandId(1)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name("SomeOtherGood")
                .price(20f)
                .brandId(2)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }

    private List<Good> givenGoodsWithGroupIdAndBrandId(Integer groupId, Integer brandId) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name("SomeGood")
                .price(15f)
                .brandId(brandId)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name("SomeOtherGood")
                .price(20f)
                .brandId(brandId)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }

    private List<Good> givenGoodsWithGroupIdAndBrandIdAndName(Integer groupId, Integer brandId, String name) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name(name)
                .price(15f)
                .brandId(brandId)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name(name + " other")
                .price(20f)
                .brandId(brandId)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }


    private List<Good> givenGoodsWithGroupIdAndName(Integer groupId, String name) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name(name)
                .price(15f)
                .brandId(1)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name(name + " other")
                .price(20f)
                .brandId(2)
                .groupId(groupId)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }

    private List<Good> givenGoodsWithBrandId(Integer brandId) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name("someName")
                .price(15f)
                .brandId(brandId)
                .groupId(1)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name("otherName")
                .price(20f)
                .brandId(brandId)
                .groupId(15)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }


    private List<Good> givenGoodsWithName(String someName) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name(someName)
                .price(15f)
                .brandId(2)
                .groupId(1)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name(someName + " other")
                .price(20f)
                .brandId(5)
                .groupId(2)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }

    private List<Good> givenGoodsWithBrandIdAndName(Integer brandId, String name) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name(name)
                .price(15f)
                .brandId(brandId)
                .groupId(1)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name(name + " other")
                .price(20f)
                .brandId(brandId)
                .groupId(2)
                .unit("100")
                .inPackage(20f)
                .barCode("someOtherCode")
                .weight(2000f)
                .build());
        return goods;
    }
}
