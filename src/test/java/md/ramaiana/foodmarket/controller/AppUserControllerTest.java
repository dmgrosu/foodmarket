package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.controller.dto.authorization.LoginRequestDto;
import md.ramaiana.foodmarket.controller.dto.authorization.SignUpRequestDto;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.service.AppUserService;
import md.ramaiana.foodmarket.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
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
    @MockitoBean
    private AppUserService appUserServiceMock;
    @MockitoBean
    private TokenService tokenServiceMock;
    @MockitoBean
    private AuthenticationManager authenticationManagerMock;
    ObjectMapper objectMapper = new ObjectMapper();

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
                .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                .content(givenUserRegisterDtoInJson("email", "passwd")))
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

    private String givenUserDtoInJson(String email, String passwd) {
        LoginRequestDto loginRequest = new LoginRequestDto(email, passwd);
        return objectMapper.writeValueAsString(loginRequest);
    }

    private String givenUserRegisterDtoInJson(String email, String passwd) {
        SignUpRequestDto request = new SignUpRequestDto(email, passwd, null);
        return objectMapper.writeValueAsString(request);
    }

}
