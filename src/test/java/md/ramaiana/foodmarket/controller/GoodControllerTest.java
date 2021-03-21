package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.GoodGroup;
import md.ramaiana.foodmarket.service.GoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.Nullable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GoodControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodService goodServiceMock;

    @WithMockUser("spring")
    @Test
    void test_getAllGroups_listReturned() throws Exception {
        //ARRANGE
        givenGroupsForParent(null);
        //ACT & ASSERT
        mockMvc.perform(get("/good/listGroups")
                .param("group_id", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getAllGroupsForParent_listReturned() throws Exception {
        //ARRANGE
        Integer parentGroupId = 8;
        givenGroupsForParent(parentGroupId);
        //ACT & ASSERT
        mockMvc.perform(get("/good/listGroups")
                .param("parentGroupId", parentGroupId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
    }

    @WithMockUser("spring")
    @Test
    void test_getGoodsForParent_listReturned() throws Exception {
        //ARRANGE
        Integer someGroupId = 8;
        givenGoodsFilteredBy(someGroupId, null, null);
        //ACT & ASSERT
        mockMvc.perform(get("/good/listGoods")
                .param("groupId", someGroupId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods[0].id").value("1"))
                .andExpect(jsonPath("$.goods[0].price").value("15.0"))
                .andExpect(jsonPath("$.goods[0].groupId").value(someGroupId))
                .andExpect(jsonPath("$.goods[0].brandId").value("1"))
                .andExpect(jsonPath("$.goods[0].name").value("someName"))
                .andExpect(jsonPath("$.goods[0].barCode").value("111222333444"))
                .andExpect(jsonPath("$.goods[0].package").value("20.0"))
                .andExpect(jsonPath("$.goods[0].weight").value("1000.0"))
                .andExpect(jsonPath("$.goods[0].unit").value("кг"))
                .andExpect(jsonPath("$.goods[1].groupId").value(someGroupId))
                .andExpect(jsonPath("$.goods[1].name").value("someOtherName"));
    }

//    @WithMockUser("spring")
//    @Test
//    void test_getFilteredGoods_withGroupIdAndBrandIdAndName() throws Exception {
//        //ARRANGE
//        List<Good> givenGoods = givenGoodsWithGroupIdAndBrandIdAndName(8, 5, "someName");
//        List<GoodGroup> givenGroups = givenGroupsForParent(8);
//        when(goodServiceMock.findGoodsFiltered(eq(8), eq(5), eq("someName")))
//                .thenReturn(givenGoods);
//        when(goodServiceMock.getGroupsHierarchy(eq(8)))
//                .thenReturn(givenGroups);
//        //ACT & ASSERT
//        mockMvc.perform(get("/good/listGroups")
//                .param("group_id", "8")
//                .param("brand_id", "5")
//                .param("name", "someName"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.goods").isArray())
//                .andExpect(jsonPath("$.goods[0].name").value("someName"))
//                .andExpect(jsonPath("$.goods[1].name").value("someName other"))
//                .andExpect(jsonPath("$.groups[0].name").value("someGroupName"))
//                .andExpect(jsonPath("$.groups[1].name").value("someOtherGroupName"));
//    }

    private void givenGroupsForParent(Integer parentGroupId) {
        List<GoodGroup> groups = new ArrayList<>();
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
        when(goodServiceMock.getGroupsHierarchy(eq(parentGroupId)))
                .thenReturn(groups);
    }

    private void givenGoodsFilteredBy(Integer groupId, @Nullable Integer brandId, @Nullable String name) {
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name(name == null ? "someName" : name)
                .price(15f)
                .brandId(brandId == null ? 1 : brandId)
                .groupId(groupId)
                .unit("кг")
                .inPackage(20f)
                .barCode("111222333444")
                .weight(1000f)
                .build());
        goods.add(Good.builder()
                .id(2)
                .name(name == null ? "someOtherName" : name)
                .price(20f)
                .brandId(brandId == null ? 2 : brandId)
                .groupId(groupId)
                .unit("buc")
                .inPackage(20f)
                .barCode("222333444555")
                .weight(2000f)
                .build());
        when(goodServiceMock.findGoodsFiltered(eq(groupId), eq(brandId), eq(name)))
                .thenReturn(goods);
    }

}
