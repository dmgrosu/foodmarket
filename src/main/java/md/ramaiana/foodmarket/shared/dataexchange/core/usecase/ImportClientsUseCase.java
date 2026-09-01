package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.client.data.ClientAddressEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientPhoneEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ClientsDataDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpAddressDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpClientDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpPhoneDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ImportClientsUseCase {

    private static final String CLIENTS_DATA_FILE = "clients-data.xml";
    /**
     * Width of {@code client.idno}. The ERP export contains fiscal codes that overflow it; they are
     * skipped rather than truncated, because a truncated fiscal code identifies a different company.
     */
    private static final int IDNO_MAX_LENGTH = 13;

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final Unmarshaller unmarshaller;
    private final ClientRepository clientRepository;

    public void execute() {
        Path filePath = Path.of(exchangeFolderPath).resolve(CLIENTS_DATA_FILE);
        try {
            ClientsDataDto clientsData = (ClientsDataDto) unmarshaller.unmarshal(getSource(filePath));
            List<ClientEntity> clients = toClients(clientsData.getClients());
            clientRepository.saveAll(clients);
            deleteFile(filePath);
            log.info("Imported {} clients", clients.size());
        } catch (FileNotFoundException ex) {
            log.warn("Skip clients import - no {} file found", CLIENTS_DATA_FILE);
        } catch (Exception ex) {
            log.error("Error while importing clients: {}", ex.getMessage(), ex);
        }
    }

    @NonNull
    private StreamSource getSource(Path file) throws FileNotFoundException {
        return new StreamSource(new FileInputStream(file.toFile()));
    }

    private void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error while deleting file {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * The ERP export is not clean enough to hand straight to a batch insert: some rows carry a fiscal
     * code the {@code client.idno} column cannot hold, and the same code can appear more than once.
     * Either one aborts the whole batch and loses every valid client, so both are dropped here, with
     * the last occurrence of a repeated code winning.
     */
    private List<ClientEntity> toClients(List<ErpClientDto> erpClients) {
        if (erpClients == null) {
            return List.of();
        }
        Map<String, ErpClientDto> byIdno = new LinkedHashMap<>();
        int skipped = 0;
        for (ErpClientDto dto : erpClients) {
            if (!hasStorableIdno(dto)) {
                skipped++;
                continue;
            }
            byIdno.put(dto.getIdno(), dto);
        }
        int repeated = erpClients.size() - skipped - byIdno.size();
        if (skipped > 0) {
            log.warn("Skipped {} clients with a blank idno or one longer than {} characters",
                    skipped, IDNO_MAX_LENGTH);
        }
        if (repeated > 0) {
            log.warn("Collapsed {} clients repeating an idno already present in the file", repeated);
        }
        return byIdno.values().stream()
                .map(this::toClient)
                .toList();
    }

    private boolean hasStorableIdno(ErpClientDto dto) {
        String idno = dto.getIdno();
        return idno != null && !idno.isBlank() && idno.length() <= IDNO_MAX_LENGTH;
    }

    private ClientEntity toClient(ErpClientDto dto) {
        return clientRepository.findByIdnoAndDeletedAtIsNull(dto.getIdno())
                .map(it -> new ClientEntity(
                        it.getId(),
                        dto.getName(),
                        dto.getIdno(),
                        dto.getEmail(),
                        it.getCreatedAt(),
                        it.getDeletedAt(),
                        toAddresses(dto.getAddresses()),
                        toPhones(dto.getPhones())
                ))
                .orElse(new ClientEntity(
                        dto.getName(),
                        dto.getIdno(),
                        dto.getEmail(),
                        toAddresses(dto.getAddresses()),
                        toPhones(dto.getPhones())
                ));
    }

    private Set<ClientPhoneEntity> toPhones(List<ErpPhoneDto> phones) {
        return phones == null ?
                Set.of() :
                phones.stream()
                        .map(this::toPhone)
                        .collect(Collectors.toSet());
    }

    private ClientPhoneEntity toPhone(ErpPhoneDto dto) {
        return new ClientPhoneEntity(dto.getNumber(), dto.getName());
    }

    private Set<ClientAddressEntity> toAddresses(List<ErpAddressDto> addresses) {
        return addresses == null ?
                Set.of() :
                addresses.stream()
                        .map(this::toAddress)
                        .collect(Collectors.toSet());
    }

    private ClientAddressEntity toAddress(ErpAddressDto dto) {
        return new ClientAddressEntity(
                dto.getType(),
                dto.getFullAddress(),
                dto.getDescription()
        );
    }

}
