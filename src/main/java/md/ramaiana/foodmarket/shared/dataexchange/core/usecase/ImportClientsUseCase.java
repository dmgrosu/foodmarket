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
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ImportClientsUseCase {

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final Unmarshaller unmarshaller;
    private final ClientRepository clientRepository;

    public void execute() {
        final String clientFileName = "clients-data.xml";
        try {
            String filePath = exchangeFolderPath + clientFileName;
            ClientsDataDto clientsData = (ClientsDataDto) unmarshaller.unmarshal(getSource(filePath));
            clientRepository.saveAll(toClients(clientsData.getClients()));
            deleteFile(filePath);
            log.info("Clients imported successfully");
        } catch (FileNotFoundException ex) {
            log.warn("Skip clients import - no {} file found", clientFileName);
        } catch (Exception ex) {
            log.error("Error while importing clients: {}", ex.getMessage(), ex);
        }
    }

    @NonNull
    private StreamSource getSource(String file) throws FileNotFoundException {
        return new StreamSource(new FileInputStream(file));
    }

    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error while deleting file {}: {}", filePath, e.getMessage());
        }
    }

    private List<ClientEntity> toClients(List<ErpClientDto> erpClients) {
        return erpClients == null ?
                null :
                erpClients.stream()
                        .map(this::toClient)
                        .toList();
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
