package md.ramaiana.foodmarket.domain.product.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.data.BalanceEntity;
import md.ramaiana.foodmarket.domain.product.data.BalanceRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpBalanceDto;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class BalancesUpdateUseCase {

    private final BalanceRepository balanceRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;

    public void execute(List<ErpBalanceDto> erpBalances) {
        List<BalanceEntity> balances = erpBalances.stream().map(this::mapToEntity).toList();
        balanceRepository.updateBalances(balances);
    }

    private BalanceEntity mapToEntity(ErpBalanceDto dto) {
        AggregateReference<StorageEntity, Integer> storage = storageRepository.getByErpCode(dto.getStorageErpCode());
        AggregateReference<ProductEntity, Integer> product = productRepository.getByErpCode(dto.getProductErpCode());
        return new BalanceEntity(storage, product, dto.getQuantity());
    }

}
