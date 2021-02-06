package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.model.AppRole;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class AppUserServiceTest {

    @Mock
    private AppUserDao userDaoMock;
    @InjectMocks
    private AppUserService appUserService;

    @Test
    void loadUserByUsername() {
        // ARRANGE
        Set<AppRole> someRoles = new HashSet<>();
        someRoles.add(AppRole.builder()
                .userId(123)
                .role(Role.USER)
                .build());
        when(userDaoMock.findByEmail(eq("someEmail"))).thenReturn(AppUser.builder()
                .id(123)
                .email("someEmail")
                .passwd("passwd")
                .roles(someRoles)
                .build());

        // ACT
        UserDetails actualUser = appUserService.loadUserByUsername("someEmail");

        // ASSERT
        assertThat(actualUser.getUsername()).isEqualTo("someEmail");
        assertThat(actualUser.getAuthorities()).extracting("role").contains("USER");
    }
}
