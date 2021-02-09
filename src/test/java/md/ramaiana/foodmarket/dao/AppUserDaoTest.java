package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataJdbcConfig.class)
class AppUserDaoTest {

    @Autowired
    private AppUserDao appUserDao;

    @Test
    void test_findByEmail_userReturned() {
        // ARRANGE
        AppUser expectedUser = someUserSaved("userEmail", "passwd");
        // ACT
        AppUser actualUser = appUserDao.findByEmail("userEmail").get();
        // ASSERT
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getEmail()).isEqualTo(expectedUser.getEmail());
        assertThat(actualUser.getPasswd()).isEqualTo(expectedUser.getPasswd());
        assertThat(actualUser.getRoles()).extracting("role").contains(Role.USER);
    }

    @Test
    void test_existsByEmail_found_trueReturned() {
        // ARRANGE
        AppUser someUser = someUserSaved("userEmail", "passwd");
        // ACT
        Boolean found = appUserDao.existsByEmail("userEmail");
        // ASSERT
        assertThat(found).isTrue();
    }

    @Test
    void test_existsByEmail_notFound_falseReturned() {
        // ARRANGE
        AppUser someUser = someUserSaved("email", "passwd");
        // ACT
        Boolean found = appUserDao.existsByEmail("userEmail");
        // ASSERT
        assertThat(found).isFalse();
    }

    @Test
    void test_saveUser_userWithIdReturned() {
        // ARRANGE
        AppUser someUser = AppUser.builder()
                .email("email")
                .passwd("passwd")
                .createdAt(OffsetDateTime.now())
                .build();
        someUser.addRole(Role.USER);
        // ACT
        AppUser actualUser = appUserDao.save(someUser);
        // ASSERT
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getId()).isNotNull();
        assertThat(actualUser.getClient()).isNull();
    }

    private AppUser someUserSaved(String email, String passwd) {
        AppUser someUser = AppUser.builder()
                .email(email)
                .passwd(passwd)
                .createdAt(OffsetDateTime.now())
                .build();
        someUser.addRole(Role.USER);
        return appUserDao.save(someUser);
    }

}
