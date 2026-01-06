package md.ramaiana.foodmarket.controller;


import md.ramaiana.foodmarket.model.Brand;
import md.ramaiana.foodmarket.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandServiceMock;


    @Test
    @WithMockUser("spring")
    void test_getAllBrands() throws Exception {
        //ARRANGE
        List<Brand> brands = new ArrayList<>();
        brands.add(Brand.builder()
                .id(1)
                .name("someName")
                .erpCode("123456")
                .createdAt(OffsetDateTime.now())
                .build());
        when(brandServiceMock.getAllBrands())
                .thenReturn(brands);
        //ACT
        mockMvc.perform(get("/brand/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("someName"));
    }
}
