package md.ramaiana.foodmarket.domain.storage.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.storage.core.response.StorageResponse;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class StorageSearchUseCase {

    private final StorageRepository repository;

    public StorageEntity findByErpCode(String erpCode) {
        return repository.findByErpCode(erpCode).orElseThrow(
                () -> new NotFoundException("Storage not found by erp code: " + erpCode)
        );
    }

    public List<StorageResponse> findAll() {
        List<StorageResponse> storages = new ArrayList<>();
        for (StorageEntity entity : repository.findAll()) {
            storages.add(new StorageResponse(entity));
        }
        return storages;
    }

}
