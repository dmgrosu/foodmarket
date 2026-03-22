package md.ramaiana.foodmarket.domain.storage.core.response;

import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;

public record StorageResponse(
        Integer id, String name
) {
    public StorageResponse(StorageEntity enty) {
        this(enty.getId(), enty.getName());
    }
}
