package md.ramaiana.foodmarket.domain.brand.data;

import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.BadSqlGrammarException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BrandRepositoryTest {

  @Autowired
  BrandRepository repository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void should_page_and_sort_brands() {
    repository.save(new BrandEntity(null, "Cola", "code-cola", Instant.now(), null));
    repository.save(new BrandEntity(null, "Bread", "code-bread", Instant.now(), null));
    repository.save(new BrandEntity(null, "Apple", "code-apple", Instant.now(), null));
    repository.save(new BrandEntity(null, "Deleted", "code-deleted", Instant.now(), Instant.now()));

    Page<BrandEntity> firstPage = repository.search(null, PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
    assertThat(firstPage.getContent()).extracting(BrandEntity::getName).containsExactly("Apple", "Bread");

    Page<BrandEntity> secondPage = repository.search(null, PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(secondPage.getContent()).extracting(BrandEntity::getName).containsExactly("Cola");

    Page<BrandEntity> descPage = repository.search(null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name")));
    assertThat(descPage.getContent()).extracting(BrandEntity::getName).containsExactly("Cola", "Bread", "Apple");
  }

  @Test
  void should_filter_brands_by_name() {
    repository.save(new BrandEntity(null, "Cola", "code-cola-2", Instant.now(), null));
    repository.save(new BrandEntity(null, "Coconut", "code-coconut", Instant.now(), null));
    repository.save(new BrandEntity(null, "Bread", "code-bread-2", Instant.now(), null));

    Page<BrandEntity> page = repository.search("co", PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent()).extracting(BrandEntity::getName).containsExactlyInAnyOrder("Cola", "Coconut");
  }

  @Test
  void framework_blocks_injection_but_not_arbitrary_columns() {
    // Documents why AdminBrandSearchUseCase whitelists the sort property rather than trusting
    // the persistence layer. Spring Data's only guard here is SqlSort.validate's character
    // filter, which rejects an injection payload...
    assertThatThrownBy(() -> repository.search(null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "bogus; DROP TABLE brand--"))))
        .isInstanceOf(IllegalArgumentException.class);

    // ...but a syntactically clean unknown property is interpolated straight into ORDER BY and
    // only fails at the database. Nothing checks it against the entity's properties, so the
    // application-level whitelist is what actually constrains this.
    assertThatThrownBy(() -> repository.search(null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "notAProperty"))))
        .isInstanceOf(BadSqlGrammarException.class);
  }
}
