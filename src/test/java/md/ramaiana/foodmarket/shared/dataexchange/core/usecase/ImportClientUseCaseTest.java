package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application.yml")
class ImportClientUseCaseTest {

    @TestConfiguration
    static class TextConfig {
        @Value("${dataFolderPath}")
        private String folderPath;

        @Bean
        public ImportClientUseCase useCase() {
            DataExchangeConfig config = new DataExchangeConfig();
            ImportClientUseCase useCase = new ImportClientUseCase(config.unmarshaller(), clientRepository());
            useCase.setExchangeFolderPath(folderPath);
            return useCase;
        }

        @Bean
        public ClientRepository clientRepository() {
            return mock(ClientRepository.class);
        }
    }

    @Autowired
    ImportClientUseCase useCase;
    @Autowired
    ClientRepository clientRepository;
    @Captor
    ArgumentCaptor<List<ClientEntity>> clientsCaptor;

    @AfterEach
    void tearDown() {
        reset(clientRepository);
    }

    @Test
    void should_import_new_clients() {
        useCase.execute();

        verify(clientRepository).saveAll(clientsCaptor.capture());
        List<ClientEntity> actualClients = clientsCaptor.getValue();
        assertThat(actualClients).hasSize(2)
                .extracting(ClientEntity::getId,
                        ClientEntity::getName,
                        ClientEntity::getIdno,
                        ClientEntity::getEmail
                )
                .containsExactly(
                        tuple(null, "S.R.L Kaufland", "111111111111", "test@dot.md"),
                        tuple(null, "IMENSITATE SRL", "22222222222", "email1@test.md;email2@test.md")
                );
    }

    @Test
    void should_update_existing_clients() {
        ClientEntity existingClient1 = new ClientEntity(
                1,
                "Old Name 1",
                "111111111111",
                "old@email.md",
                Instant.now().minusSeconds(3600),
                null,
                List.of(),
                List.of()
        );
        ClientEntity existingClient2 = new ClientEntity(
                2,
                "Old Name 2",
                "22222222222",
                "old2@email.md",
                Instant.now().minusSeconds(7200),
                null,
                List.of(),
                List.of()
        );
        when(clientRepository.findByIdnoAndDeletedAtIsNull("111111111111"))
                .thenReturn(Optional.of(existingClient1));
        when(clientRepository.findByIdnoAndDeletedAtIsNull("22222222222"))
                .thenReturn(Optional.of(existingClient2));

        useCase.execute();

        verify(clientRepository).saveAll(clientsCaptor.capture());
        List<ClientEntity> actualClients = clientsCaptor.getValue();
        assertThat(actualClients).hasSize(2)
                .extracting(ClientEntity::getId,
                        ClientEntity::getName,
                        ClientEntity::getIdno,
                        ClientEntity::getEmail
                )
                .containsExactly(
                        tuple(1, "S.R.L Kaufland", "111111111111", "test@dot.md"),
                        tuple(2, "IMENSITATE SRL", "22222222222", "email1@test.md;email2@test.md")
                );
    }

}