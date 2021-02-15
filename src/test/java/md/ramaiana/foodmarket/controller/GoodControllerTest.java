package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.service.GoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

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
    void test_getAllGoods() throws Exception{
        //ARRANGE
        Brand brand = Brand.builder()
                .id(1)
                .name("SomeBrand")
                .build();
        List<Good> goods = new ArrayList<>();
        goods.add(Good.builder()
                .id(1)
                .name("SomeGood")
                .price(15f)
                .brandId(brand.getId())
                .groupId(15)
                .unit("100")
                .inPackage(20f)
                .barCode("someCode")
                .weight(1000f)
                .build());
        when(goodServiceMock.getAllGoods())
                .thenReturn(goods);
        //ACT
        mockMvc.perform(get("/good/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goods").isArray())
                .andExpect(jsonPath("$.goods[0].name").value("SomeGood"));
        //ASSERT
    }
}
