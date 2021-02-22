package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.service.ClientNotFoundException;
import md.ramaiana.foodmarket.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClientService clientServiceMock;

    @Test
    void test_getIdByIdno_clientReturned() throws Exception {
        // ARRANGE
        String someIdno = "123456";
        when(clientServiceMock.findClientByIdno(eq(someIdno)))
                .thenReturn(Client.builder()
                        .id(123)
                        .name("someClientName")
                        .idno(someIdno)
                        .build());
        // ACT
        mockMvc.perform(get("/client/findByIdno")
                .param("idno", someIdno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.name").value("someClientName"))
                .andExpect(jsonPath("$.idno").value("123456"));
    }

    @Test
    void test_getIdByIdno_clientNotFound_responseWithErrorReturned() throws Exception {
        // ARRANGE
        String someIdno = "123456";
        when(clientServiceMock.findClientByIdno(eq(someIdno)))
                .thenThrow(new ClientNotFoundException("Client not found"));
        // ACT
        mockMvc.perform(get("/client/findByIdno")
                .param("idno", someIdno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].code").value("CLIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].description").value("Client not found"));
    }
}
