package md.ramaiana.foodmarket.domain.auth.data;

import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
class AppUserRepositoryTest {

  @Autowired
  AppUserRepository repository;

  private String email(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@example.com";
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void should_page_and_sort_users() {
    repository.save(new AppUserEntity(email("cola"), "hash", UserState.PENDING_CONFIRMATION, Language.RU));
    repository.save(new AppUserEntity(email("bread"), "hash", UserState.PENDING_CONFIRMATION, Language.RU));
    repository.save(new AppUserEntity(email("apple"), "hash", UserState.PENDING_CONFIRMATION, Language.RU));

    Page<AppUserEntity> firstPage = repository.search(null, null,
        PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "email")));

    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
  }

  @Test
  void should_filter_users_by_email_and_state() {
    String confirmed = email("confirmed-user");
    String pending = email("pending-user");
    repository.save(new AppUserEntity(confirmed, "hash", UserState.CONFIRMED, Language.RU));
    repository.save(new AppUserEntity(pending, "hash", UserState.PENDING_CONFIRMATION, Language.RU));

    Page<AppUserEntity> byEmail = repository.search("confirmed-user", null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email")));
    assertThat(byEmail.getContent()).extracting(AppUserEntity::getEmail).containsExactly(confirmed);

    Page<AppUserEntity> byState = repository.search(null, UserState.CONFIRMED,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email")));
    assertThat(byState.getContent()).extracting(AppUserEntity::getEmail).containsExactly(confirmed);
  }

  @Test
  void should_load_child_collections_through_paged_search() {
    // The paged search goes through JdbcAggregateOperations, so @MappedCollection children
    // (userRoles) are loaded as part of the aggregate rather than left empty.
    String withRole = email("with-role");
    AppUserEntity user = new AppUserEntity(withRole, "hash", UserState.ACTIVE, Language.RU);
    user.addRole(Role.USER);
    repository.save(user);

    Page<AppUserEntity> page = repository.search("with-role", null,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email")));

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().getFirst().getRoles()).containsExactly(Role.USER);
  }
}
