package md.ramaiana.foodmarket.domain.product.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.product.data.BalanceEntity;
import md.ramaiana.foodmarket.domain.product.data.BalanceRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.storage.core.usecase.StorageSearchUseCase;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpBalanceDto;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;
import java.util.Optional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class BalancesUpdateUseCase {

    private final BalanceRepository balanceRepository;
    private final ProductFindByErpCodeUseCase productSearch;
    private final StorageSearchUseCase storageSearch;

    public void execute(List<ErpBalanceDto> erpBalances) {
        List<BalanceEntity> balances = erpBalances.stream()
                .map(this::mapToEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if (!balances.isEmpty()) {
            balanceRepository.updateBalances(balances);
        }
    }

    private Optional<BalanceEntity> mapToEntity(ErpBalanceDto dto) {
        try {
            AggregateReference<StorageEntity, Integer> storage = AggregateReference.to(
                    storageSearch.findByErpCode(dto.getStorageCode()).getId()
            );
            AggregateReference<ProductEntity, Integer> product = AggregateReference.to(
                    productSearch.execute(dto.getProductCode()).getId()
            );
            return Optional.of(new BalanceEntity(storage, product, dto.getQuantity()));
        } catch (Exception e) {
            log.error("Error while mapping balance {}: {}", dto, e.getMessage());
            return Optional.empty();
        }
    }

}
