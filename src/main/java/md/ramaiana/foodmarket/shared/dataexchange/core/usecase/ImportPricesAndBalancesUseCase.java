package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.price.core.usecase.PricesUpdateUseCase;
import md.ramaiana.foodmarket.domain.product.core.usecase.BalancesUpdateUseCase;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.BalanceDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ImportPricesAndBalancesUseCase {

    private static final String BALANCES_DATA_FILE = "balances-data.xml";

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final PricesUpdateUseCase pricesUpdate;
    private final BalancesUpdateUseCase balancesUpdate;
    private final Unmarshaller unmarshaller;


    public void execute() throws IOException {
        String filePath = exchangeFolderPath + BALANCES_DATA_FILE;
        BalanceDataDto balanceDataDto = (BalanceDataDto) unmarshaller.unmarshal(getSource(filePath));
        pricesUpdate.execute(balanceDataDto.getPrices());
        balancesUpdate.execute(balanceDataDto.getBalances());
    }

    @NonNull
    private StreamSource getSource(String file) throws FileNotFoundException {
        return new StreamSource(new FileInputStream(file));
    }

}
