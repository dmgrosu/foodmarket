package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.product.core.usecase.BalancesUpdateUseCase;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.BalanceDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ImportBalancesUseCase {

    private static final String BALANCES_DATA_FILE = "balances-data.xml";

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final BalancesUpdateUseCase balancesUpdate;
    private final Unmarshaller unmarshaller;


    public void execute() {
        String filePath = exchangeFolderPath + BALANCES_DATA_FILE;
        try {
            BalanceDataDto balanceDataDto = (BalanceDataDto) unmarshaller.unmarshal(getSource(filePath));
            balancesUpdate.execute(balanceDataDto.getBalances());
            deleteFile(filePath);
        } catch (FileNotFoundException ex) {
            log.warn("Skip import balances - no {} file found", BALANCES_DATA_FILE);
        } catch (Exception ex) {
            log.error("Error while importing prices and balances: {}", ex.getMessage(), ex);
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

}
