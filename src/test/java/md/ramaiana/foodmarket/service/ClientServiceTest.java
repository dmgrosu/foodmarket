package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.model.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ClientServiceTest {

    @Mock
    private ClientDao clientDaoMock;
    @InjectMocks
    private ClientService clientService;

    @Test
    void test_findClientByIdno_found() throws Exception {
        // ARRANGE
        String someIdno = "123456";
        when(clientDaoMock.findByIdnoAndDeletedAtIsNull(eq(someIdno)))
                .thenReturn(Optional.of(Client.builder()
                        .id(123)
                        .name("someName")
                        .idno(someIdno)
                        .build()));
        // ACT
        Client actualClient = clientService.findClientByIdno(someIdno);
        // ASSERT
        assertThat(actualClient).isNotNull();
        assertThat(actualClient.getId()).isEqualTo(123);
        assertThat(actualClient.getName()).isEqualTo("someName");
    }

    @Test
    void test_findClientByIdno_notFount_exceptionThrown() throws Exception {
        // ARRANGE
        String someIdno = "123456";
        when(clientDaoMock.findByIdnoAndDeletedAtIsNull(eq(someIdno)))
                .thenReturn(Optional.empty());
        // ACT & ASSERT
        assertThatExceptionOfType(ClientNotFoundException.class)
                .isThrownBy(() -> clientService.findClientByIdno(someIdno));
    }
}
