package md.ramaiana.foodmarket.domain.product.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;

@UseCase
@RequiredArgsConstructor
public class ProductFindByErpCodeUseCase {

    private final ProductRepository repository;

    public ProductEntity execute(String erpCode) {
        return repository.findByErpCode(erpCode)
                .orElseThrow(() -> new NotFoundException("Product not found by erp code: " + erpCode));
    }

}
