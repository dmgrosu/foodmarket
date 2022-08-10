package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.proto.Authorization.*;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest requestBody) throws InvalidProtocolBufferException {
        List<Common.Error> errors = validateResetPasswordRequest(requestBody);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(buildErrorResponse(errors));
        }
        try {
            AppUser existingUser = appUserService.findByEmail(requestBody.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(String.format("User with email [%s] not found", requestBody.getEmail())));
            String resetPasswordToken = tokenService.generatePasswordResetToken(existingUser);
            appUserService.resetPassword(resetPasswordToken, existingUser);
        } catch (UserNotFoundException e) {
            log.warn(e.getMessage());
            errors.add(Common.Error.newBuilder().setCode(Common.ErrorCode.CLIENT_NOT_FOUND).setDescription(e.getMessage()).build());
            return ResponseEntity.badRequest().body(buildErrorResponse(errors));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validateResetPasswordToken")
    public ResponseEntity<?> validateResetPasswordToken(@RequestBody ResetPasswordTokenRequest request) throws InvalidProtocolBufferException {
        try {
            appUserService.validateResetPasswordToken(request.getToken());
        } catch (ResetPasswordTokenExpiredException e) {
            log.warn(e.getMessage());
            List<Common.Error> errors = new ArrayList<>();
            errors.add(Common.Error.newBuilder().setCode(Common.ErrorCode.RESET_PASSWORD_TOKEN_EXPIRED).setDescription(e.getMessage()).build());
            return ResponseEntity.badRequest().body(buildErrorResponse(errors));
        } catch (ResetPasswordTokenNotFoundException e) {
            log.warn(e.getMessage());
            List<Common.Error> errors = new ArrayList<>();
            errors.add(Common.Error.newBuilder().setCode(Common.ErrorCode.RESET_PASSWORD_TOKEN_NOT_FOUND).setDescription(e.getMessage()).build());
            return ResponseEntity.badRequest().body(buildErrorResponse(errors));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/newPassword")
    public ResponseEntity<?> setNewPassword(@RequestBody NewPasswordRequest request) {
        String resetPasswordToken = request.getToken();
        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());
        appUserService.setNewPassword(resetPasswordToken, newHashedPassword);
        return ResponseEntity.ok().build();
    }

    private List<Common.Error> validateResetPasswordRequest(ResetPasswordRequest request) {
        List<Common.Error> errors = new ArrayList<>();
        if (request.getEmail().isEmpty()) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.EMAIL_EMPTY)
                    .setDescription("Missing required user email")
                    .build());
        }
        return errors;
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
