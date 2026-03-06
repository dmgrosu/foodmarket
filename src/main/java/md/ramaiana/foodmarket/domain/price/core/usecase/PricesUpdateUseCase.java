package md.ramaiana.foodmarket.domain.price.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.price.data.PriceEntity;
import md.ramaiana.foodmarket.domain.price.data.PriceRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpPriceDto;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class PricesUpdateUseCase {

    private final PriceRepository priceRepository;
    private final StorageRepository storageRepository;
    private final ProductRepository productRepository;


    public void execute(List<ErpPriceDto> erpPrices) {
        for (ErpPriceDto erpPrice : erpPrices) {
            AggregateReference<StorageEntity, Integer> storage = storageRepository.getByErpCode(erpPrice.getStorageErpCode());
            AggregateReference<ProductEntity, Integer> product = productRepository.getByErpCode(erpPrice.getProductErpCode());
            PriceEntity price = priceRepository.findByStorageAndProductAndType(storage, product, erpPrice.getType())
                    .map(it -> it.withPrice(erpPrice.getPrice()))
                    .orElse(new PriceEntity(null, erpPrice.getType(), storage, product, erpPrice.getPrice()));
            priceRepository.save(price);
        }
    }

}
