package md.ramaiana.foodmarket.controller;

import md.ramaiana.foodmarket.model.AppRole;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.Role;
import md.ramaiana.foodmarket.model.dto.UserDto;
import md.ramaiana.foodmarket.service.AppUserService;
import md.ramaiana.foodmarket.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitri Grosu, 2/7/21
 */
@RestController
@RequestMapping("/auth")
public class AppUserController {

    private final AppUserService appUserService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserController(AppUserService appUserService,
                             TokenService tokenService,
                             AuthenticationManager authenticationManager,
                             PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPasswd())
        );
        if (authentication.isAuthenticated()) {
            AppUser appUser = (AppUser) authentication.getPrincipal();
            String token = tokenService.createToken(appUser);
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(UserDto.builder()
                            .userId(appUser.getId())
                            .email(appUser.getEmail())
                            .build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        if (appUserService.userEmailExists(userDto.getEmail())) {
            return ResponseEntity.badRequest().body("User email is not unique!");
        }
        AppUser appUser = AppUser.builder()
                .email(userDto.getEmail())
                .passwd(passwordEncoder.encode(userDto.getPasswd()))
                .createdAt(OffsetDateTime.now())
                .build();
        appUser.addRole(Role.USER);
        AppUser savedUser = appUserService.registerNewUser(appUser);
        String token = tokenService.createToken(savedUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(UserDto.builder()
                        .userId(savedUser.getId())
                        .email(savedUser.getEmail())
                        .build());
    }

}
