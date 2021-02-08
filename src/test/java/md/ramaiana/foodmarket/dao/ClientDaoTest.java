package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataJdbcConfig.class)
class ClientDaoTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    void test_findByIdnoAndDeletedAtIsNull() {
        // ARRANGE
        Client givenSavedClient = someSavedClient("123456", "someName");
        // ACT
        Client actualClient = clientDao.findByIdnoAndDeletedAtIsNull("123456").get();
        // ASSERT
        assertThat(actualClient.getId()).isEqualTo(givenSavedClient.getId());
        assertThat(actualClient.getName()).isEqualTo(givenSavedClient.getName());
        assertThat(actualClient.getDeletedAt()).isNull();
    }

    @Test
    void test_findByIdnoAndDeletedAtIsNull_notFound() {
        // ARRANGE
        Client givenSavedClient = someDeletedClient("123456", "someName");
        // ACT
        Optional<Client> actualClient = clientDao.findByIdnoAndDeletedAtIsNull("123456");
        // ASSERT
        assertThat(actualClient.isPresent()).isFalse();
    }

    private Client someSavedClient(String idno, String name) {
        return clientDao.save(Client.builder()
                .idno(idno)
                .name(name)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Client someDeletedClient(String idno, String name) {
        return clientDao.save(Client.builder()
                .idno(idno)
                .name(name)
                .createdAt(OffsetDateTime.now().minusDays(10))
                .deletedAt(OffsetDateTime.now().minusDays(2))
                .build());
    }
}
