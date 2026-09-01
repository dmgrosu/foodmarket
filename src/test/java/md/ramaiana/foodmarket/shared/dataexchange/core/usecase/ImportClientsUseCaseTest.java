package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import md.ramaiana.foodmarket.config.DataExchangeConfig;
import md.ramaiana.foodmarket.domain.client.data.ClientAddressEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.enums.AddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ImportClientsUseCaseTest {

    private static final String EXCHANGE_FOLDER = "src/test/resources/dataExchange";

    @Mock
    ClientRepository clientRepository;
    @Captor
    ArgumentCaptor<List<ClientEntity>> clientsCaptor;
    ImportClientsUseCase useCase;
    Path path = Paths.get(EXCHANGE_FOLDER, "clients-data.xml");

    @BeforeEach
    void setUp() throws Exception {
        useCase = new ImportClientsUseCase(new DataExchangeConfig().unmarshaller(), clientRepository);
        // Deliberately without a trailing separator - the importer must not depend on one.
        useCase.setExchangeFolderPath(EXCHANGE_FOLDER);

        Files.createDirectories(path.getParent());
        String xml = """
                <?xml version="1.0" encoding="Windows-1251"?>
                <clients-data>
                    <clients>
                        <client name="S.R.L Kaufland" idno="111111111111">
                            <address type="LEGAL" address="Chisinau, Mircea cel Batran 1" descr="Sediu central"/>
                            <phone number="022111222" name="Office"/>
                            <email>test@dot.md</email>
                        </client>
                        <client name="IMENSITATE SRL" idno="22222222222">
                            <email>email1@test.md;email2@test.md</email>
                        </client>
                    </clients>
                </clients-data>""";
        Files.writeString(path, xml);
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
        assertThat(actualClients.getFirst().getAddresses())
                .extracting(
                        ClientAddressEntity::getType,
                        ClientAddressEntity::getFullAddress,
                        ClientAddressEntity::getDescription)
                .containsExactly(
                        tuple(AddressType.LEGAL, "Chisinau, Mircea cel Batran 1", "Sediu central")
                );
        assertThat(Files.exists(path)).isFalse();
    }

    @Test
    void should_update_existing_clients() {
        ClientEntity existingClient1 = new ClientEntity(
                1,
                "Old Name 1",
                "111111111111",
                "old@email.md",
                "OLD-CODE-1",
                Instant.now().minusSeconds(3600),
                null,
                Set.of(),
                Set.of()
        );
        ClientEntity existingClient2 = new ClientEntity(
                2,
                "Old Name 2",
                "22222222222",
                "old2@email.md",
                "OLD-CODE-2",
                Instant.now().minusSeconds(7200),
                null,
                Set.of(),
                Set.of()
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