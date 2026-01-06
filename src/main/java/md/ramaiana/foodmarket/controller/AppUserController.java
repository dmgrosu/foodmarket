package md.ramaiana.foodmarket.controller;

import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.controller.dto.ClientDto;
import md.ramaiana.foodmarket.controller.dto.authorization.LoginRequestDto;
import md.ramaiana.foodmarket.controller.dto.authorization.LoginResponseDto;
import md.ramaiana.foodmarket.controller.dto.authorization.SignUpRequestDto;
import md.ramaiana.foodmarket.controller.dto.authorization.UserDto;
import md.ramaiana.foodmarket.controller.dto.common.ErrorCode;
import md.ramaiana.foodmarket.controller.dto.common.ErrorDto;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.service.AppUserService;
import md.ramaiana.foodmarket.service.ClientNotFoundException;
import md.ramaiana.foodmarket.service.ClientService;
import md.ramaiana.foodmarket.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitri Grosu, 2/7/21
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AppUserController {

    private final AppUserService appUserService;
    private final ClientService clientService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserController(AppUserService appUserService,
                             ClientService clientService,
                             TokenService tokenService,
                             AuthenticationManager authenticationManager,
                             PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.clientService = clientService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) throws ClientNotFoundException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );
        if (authentication.isAuthenticated()) {
            AppUser appUser = (AppUser) authentication.getPrincipal();
            return ResponseEntity.ok(toSuccessfulLoginResponse(appUser));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto dto) throws ClientNotFoundException {
        List<ErrorDto> errors = validateSignUpRequest(dto);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        AppUser appUser = AppUser.builder()
                .email(dto.email())
                .passwd(passwordEncoder.encode(dto.password()))
                .createdAt(OffsetDateTime.now())
                .build();
        appUser.addRole(Role.USER);
        AppUser savedUser = appUserService.registerNewUser(appUser);
        return ResponseEntity.ok(toSuccessfulLoginResponse(savedUser));
    }

    private List<ErrorDto> validateSignUpRequest(SignUpRequestDto signUpRequest) {
        List<ErrorDto> errors = new ArrayList<>();
        if (signUpRequest.email().isEmpty()) {
            errors.add(new ErrorDto(
                    ErrorCode.EMAIL_EMPTY,
                    "Missing required user email"
            ));
        }
        if (signUpRequest.password().isEmpty()) {
            errors.add(new ErrorDto(
                    ErrorCode.PASSWORD_EMPTY,
                    "Missing required password"
            ));
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        if (appUserService.userEmailExists(signUpRequest.email())) {
            errors.add(new ErrorDto(
                    ErrorCode.EMAIL_EXISTS,
                    String.format("User [%s] already exists", signUpRequest.email())
            ));
        }
        return errors;
    }

    private LoginResponseDto toSuccessfulLoginResponse(AppUser appUser) throws ClientNotFoundException {
        UserDto userDto = toUserDto(appUser);
        String token = tokenService.createToken(appUser);
        int tokenTtl = tokenService.getTOKEN_VALIDITY() / 1000;
        return new LoginResponseDto(userDto, token, tokenTtl);
    }

    private UserDto toUserDto(AppUser appUser) throws ClientNotFoundException {
        if (appUser.hasClient()) {
            Client userClient = clientService.getClientById(appUser.getClient().getId());
            return new UserDto(
                    appUser.getId(),
                    appUser.getEmail(),
                    buildClientProtoFromClient(userClient)
            );
        }
        return new UserDto(appUser.getId(), appUser.getEmail(), null);
    }

    private ClientDto buildClientProtoFromClient(Client client) {
        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getIdno()
        );
    }

}
