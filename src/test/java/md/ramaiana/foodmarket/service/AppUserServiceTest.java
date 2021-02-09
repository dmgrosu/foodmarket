package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.model.AppRole;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserDao userDaoMock;
    @Mock
    private ClientDao clientDaoMock;
    @InjectMocks
    private AppUserService appUserService;

    @Test
    void test_loadUserByUsername_foundUserReturned() {
        // ARRANGE
        Set<AppRole> someRoles = new HashSet<>();
        someRoles.add(AppRole.builder()
                .userId(123)
                .role(Role.USER)
                .build());
        when(userDaoMock.findByEmail(eq("someEmail"))).thenReturn(Optional.of(AppUser.builder()
                .id(123)
                .email("someEmail")
                .passwd("passwd")
                .roles(someRoles)
                .build()));
        // ACT
        UserDetails actualUser = appUserService.loadUserByUsername("someEmail");
        // ASSERT
        assertThat(actualUser.getUsername()).isEqualTo("someEmail");
        assertThat(actualUser.getAuthorities()).extracting("role").contains("USER");
    }

    @Test
    void test_registerNewUser_withoutClient_daoCalled() {
        // ARRANGE
        AppUser givenUser = AppUser.builder()
                .email("someEmail")
                .build();
        when(userDaoMock.save(any(AppUser.class))).thenReturn(givenUser);
        // ACT
        appUserService.registerNewUser(givenUser);
        // ASSERT
        verify(userDaoMock, times(1)).save(any(AppUser.class));
    }

    @Test
    void test_registerNewUser_withClient_daoCalled() {
        // ARRANGE
        AppUser givenUser = AppUser.builder()
                .email("someEmail")
                .clientId(123)
                .build();
        when(userDaoMock.save(any(AppUser.class))).thenReturn(givenUser);
        when(clientDaoMock.findById(eq(123)))
                .thenReturn(Optional.of(Client.builder()
                        .id(123)
                        .idno("123456")
                        .name("someName")
                        .build()));
        // ACT
        AppUser actualUser = appUserService.registerNewUser(givenUser);
        // ASSERT
        verify(userDaoMock, times(1)).save(any(AppUser.class));
        assertThat(actualUser.hasClient()).isTrue();
        assertThat(actualUser.getEmail()).isEqualTo("someEmail");
        assertThat(actualUser.getClient().getId()).isEqualTo(123);
        assertThat(actualUser.getClient().getIdno()).isEqualTo("123456");
    }
}
