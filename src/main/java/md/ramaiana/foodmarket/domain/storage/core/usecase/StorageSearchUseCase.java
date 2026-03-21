package md.ramaiana.foodmarket.domain.storage.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;

@UseCase
@RequiredArgsConstructor
public class StorageSearchUseCase {

    private final StorageRepository repository;

    public StorageEntity findByErpCode(String erpCode) {
        return repository.findByErpCode(erpCode).orElseThrow(
                () -> new NotFoundException("Storage not found by erp code: " + erpCode)
        );
    }

}
