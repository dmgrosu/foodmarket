package md.ramaiana.foodmarket.domain.client.data;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import md.ramaiana.foodmarket.shared.enums.AddressType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClientRepositoryTest {

  @Autowired
  ClientRepository repository;

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void should_page_and_sort_clients() {
    repository.save(new ClientEntity("Cola Client", "1000000000001", null, Set.of(), Set.of()));
    repository.save(new ClientEntity("Bread Client", "1000000000002", null, Set.of(), Set.of()));
    repository.save(new ClientEntity("Apple Client", "1000000000003", null, Set.of(), Set.of()));

    Page<ClientEntity> firstPage = repository.search(null, null,
        PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
    assertThat(firstPage.getContent()).extracting(ClientEntity::getName)
        .containsExactly("Apple Client", "Bread Client");
  }

  @Test
  void should_filter_clients_by_name_and_idno() {
    repository.save(new ClientEntity("Cola Client", "2000000000001", null, Set.of(), Set.of()));
    repository.save(new ClientEntity("Coconut Client", "2000000000002", null, Set.of(), Set.of()));
    repository.save(new ClientEntity("Bread Client", "2000000000003", null, Set.of(), Set.of()));

    Page<ClientEntity> byName = repository.search("co", null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(byName.getTotalElements()).isEqualTo(2);

    Page<ClientEntity> byIdno = repository.search(null, "2000000000003",
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
    assertThat(byIdno.getContent()).extracting(ClientEntity::getName).containsExactly("Bread Client");
  }

  @Test
  void should_load_child_collections_through_paged_search() {
    // The paged search goes through JdbcAggregateOperations, so @MappedCollection children
    // are loaded as part of the aggregate rather than left empty.
    repository.save(new ClientEntity("With Phone", "3000000000001", "a@b.md",
        Set.of(new ClientAddressEntity(AddressType.LEGAL, "Main St 1", "hq")),
        Set.of(new ClientPhoneEntity("+37360000000", "office"))));

    Page<ClientEntity> page = repository.search("with phone", null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

    assertThat(page.getContent()).hasSize(1);
    ClientEntity found = page.getContent().getFirst();
    assertThat(found.getEmail()).isEqualTo("a@b.md");
    assertThat(found.getPhones()).extracting(ClientPhoneEntity::getNumber).containsExactly("+37360000000");
    assertThat(found.getAddresses()).extracting(ClientAddressEntity::getFullAddress).containsExactly("Main St 1");
  }
}
