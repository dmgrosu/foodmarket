package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.AppRole;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class AppUserDaoTest {

    @Autowired
    private AppUserDao appUserDao;

    @Test
    void test_findByEmail_userReturned() {
        // ARRANGE
        AppUser expectedUser = someUserSaved("userEmail", "passwd");
        // ACT
        AppUser actualUser = appUserDao.findByEmail("userEmail");
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
        Set<AppRole> roles = new HashSet<>();
        roles.add(AppRole.builder()
                .role(Role.USER)
                .build());
        AppUser someUser = AppUser.builder()
                .email("email")
                .passwd("passwd")
                .roles(roles)
                .build();
        // ACT
        AppUser actualUser = appUserDao.save(someUser);
        // ASSERT
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getId()).isNotNull();
    }

    private AppUser someUserSaved(String email, String passwd) {
        Set<AppRole> roles = new HashSet<>();
        roles.add(AppRole.builder()
                .role(Role.USER)
                .build());
        AppUser someUser = AppUser.builder()
                .email(email)
                .passwd(passwd)
                .roles(roles)
                .build();
        return appUserDao.save(someUser);
    }

}
