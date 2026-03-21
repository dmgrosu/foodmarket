package md.ramaiana.foodmarket.domain.storage.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StorageRepositoryTest {

    @Autowired
    StorageRepository repository;

    @Test
    void should_save_new_storage() {
        StorageEntity entity = repository.save(new StorageEntity(null, "name", "code1"));

        assertThat(entity).isNotNull();
        repository.delete(entity);
    }

    @Test
    void should_return_existing_storage_reference() {
        StorageEntity entity = repository.save(new StorageEntity(null, "name", "code2"));

        var ref = repository.findByErpCode("code2");

        assertThat(ref).isNotNull()
                .get()
                .extracting("id")
                .isEqualTo(entity.getId());
        repository.delete(entity);
    }
}