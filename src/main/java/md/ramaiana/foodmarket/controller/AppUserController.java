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
import md.ramaiana.foodmarket.proto.Common;
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
        List<Common.Error> errors = validateSignUpRequest(signUpRequest);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(buildErrorResponse(errors));
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

    private List<Common.Error> validateSignUpRequest(SignUpRequest signUpRequest) {
        List<Common.Error> errors = new ArrayList<>();
        if (signUpRequest.getEmail().isEmpty()) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.EMAIL_EMPTY)
                    .setDescription("Missing required user email")
                    .build());
        }
        if (signUpRequest.getPassword().isEmpty()) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.PASSWORD_EMPTY)
                    .setDescription("Missing required password")
                    .build());
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        if (appUserService.userEmailExists(signUpRequest.getEmail())) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.EMAIL_EXISTS)
                    .setDescription(String.format("User [%s] already exists", signUpRequest.getEmail()))
                    .build());
        }
        return errors;
    }

    private String buildErrorResponse(List<Common.Error> errors) throws InvalidProtocolBufferException {
        return printer.print(Common.ErrorResponse.newBuilder()
                .addAllErrors(errors)
                .build());
    }

    private String buildSuccessfulLoginResponse(AppUser appUser) throws InvalidProtocolBufferException {
        UserProto userProto = buildProtoFromAppUser(appUser);
        String token = tokenService.createToken(appUser);
        int tokenTtl = tokenService.getTOKEN_VALIDITY() / 1000;
        return printer.print(LoginResponse.newBuilder()
                .setUser(userProto)
                .setToken(token)
                .setTokenTtl(tokenTtl)
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
