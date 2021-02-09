package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.proto.Authorization.LoginRequest;
import md.ramaiana.foodmarket.proto.Authorization.LoginResponse;
import md.ramaiana.foodmarket.proto.Authorization.SignUpRequest;
import md.ramaiana.foodmarket.proto.Authorization.UserProto;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.service.AppUserService;
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

/**
 * @author Dmitri Grosu, 2/7/21
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AppUserController {

    private final AppUserService appUserService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JsonFormat.Printer printer;

    @Autowired
    public AppUserController(AppUserService appUserService,
                             TokenService tokenService,
                             AuthenticationManager authenticationManager,
                             PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws InvalidProtocolBufferException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            AppUser appUser = (AppUser) authentication.getPrincipal();
            return ResponseEntity.ok(buildSuccessfulLoginResponse(appUser));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) throws InvalidProtocolBufferException {
        if (appUserService.userEmailExists(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("User email is not unique!");
        }
        AppUser appUser = AppUser.builder()
                .email(signUpRequest.getEmail())
                .passwd(passwordEncoder.encode(signUpRequest.getPassword()))
                .createdAt(OffsetDateTime.now())
                .build();
        appUser.addRole(Role.USER);
        AppUser savedUser = appUserService.registerNewUser(appUser);
        return ResponseEntity.ok(buildSuccessfulLoginResponse(savedUser));
    }

    private String buildSuccessfulLoginResponse(AppUser appUser) throws InvalidProtocolBufferException {
        UserProto userProto = buildProtoFromAppUser(appUser);
        String token = tokenService.createToken(appUser);
        return printer.print(LoginResponse.newBuilder()
                .setUser(userProto)
                .setToken(token)
                .build());
    }

    private UserProto buildProtoFromAppUser(AppUser appUser) {
        Client userClient = appUser.getClient();
        if (userClient == null) {
            return UserProto.newBuilder()
                    .setEmail(appUser.getEmail())
                    .setId(appUser.getId())
                    .build();
        }
        return UserProto.newBuilder()
                .setEmail(appUser.getEmail())
                .setId(appUser.getId())
                .setClient(buildClientProtoFromClient(userClient))
                .build();
    }

    private Clients.Client buildClientProtoFromClient(Client client) {
        return Clients.Client.newBuilder()
                .setId(client.getId())
                .setIdno(client.getIdno())
                .setName(client.getName())
                .build();
    }

}
