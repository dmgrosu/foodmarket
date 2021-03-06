package md.ramaiana.foodmarket.controller;

import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.proto.Authorization.LoginRequest;
import md.ramaiana.foodmarket.service.AppUserService;
import md.ramaiana.foodmarket.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AppUserService appUserServiceMock;
    @MockBean
    private TokenService tokenServiceMock;
    @MockBean
    private AuthenticationManager authenticationManagerMock;

    @Test
    void test_login_tokenReturned() throws Exception {
        // ARRANGE
        givenUserAuthorized("someEmail", "somePasswd");
        // ACT & ASSERT
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenUserDtoInJson("someEmail", "somePasswd")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("someEmail"))
                .andExpect(jsonPath("$.user.id").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenTtl").value("3600"));

    }

    @Test
    void test_register_tokenReturned() throws Exception {
        // ARRANGE
        when(appUserServiceMock.userEmailExists(eq("email"))).thenReturn(false);
        when(appUserServiceMock.registerNewUser(any(AppUser.class)))
                .thenReturn(AppUser.builder()
                        .id(123)
                        .email("email")
                        .build());
        when(tokenServiceMock.createToken(any(AppUser.class)))
                .thenReturn("someLongTokenString");
        when(tokenServiceMock.getTOKEN_VALIDITY()).thenReturn(3600000);
        // ACT & ASSERT
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenUserDtoInJson("email", "passwd")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("email"))
                .andExpect(jsonPath("$.user.id").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenTtl").value("3600"));
    }

    @Test
    void test_register_emailExists_badRequest() throws Exception {
        // ARRANGE
        when(appUserServiceMock.userEmailExists(eq("email"))).thenReturn(true);
        // ACT & ASSERT
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenUserDtoInJson("email", "passwd")))
                .andExpect(status().isBadRequest());
    }

    private void givenUserAuthorized(String email, String passwd) {
        AppUser appUser = AppUser.builder()
                .id(123)
                .email(email)
                .passwd(passwd)
                .build();
        appUser.addRole(Role.USER);
        when(authenticationManagerMock.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        appUser,
                        passwd,
                        Collections.singletonList(new SimpleGrantedAuthority("USER"))
                ));
        when(tokenServiceMock.createToken(any(AppUser.class)))
                .thenReturn("someLongTokenString");
        when(tokenServiceMock.getTOKEN_VALIDITY()).thenReturn(3600000);
    }

    private String givenUserDtoInJson(String email, String passwd) throws Exception {
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(passwd)
                .build();
        return JsonFormat.printer().print(loginRequest);
    }

}
