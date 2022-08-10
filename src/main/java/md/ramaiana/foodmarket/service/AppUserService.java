package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.model.AppUser;
import md.ramaiana.foodmarket.model.ResetPasswordToken;
import md.ramaiana.foodmarket.proto.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/6/21
 */
@Service
public class AppUserService implements UserDetailsService {

    private final AppUserDao appUserDao;
    private final ClientDao clientDao;
    private final EmailService emailService;
    private final Map<String, ResetPasswordToken> tokens = new ConcurrentHashMap<>();

    @Autowired
    public AppUserService(AppUserDao appUserDao, ClientDao clientDao, EmailService emailService) {
        this.appUserDao = appUserDao;
        this.clientDao = clientDao;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email [%s] not found", email)));
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserDao.findByEmail(email);
    }

    public AppUser registerNewUser(AppUser appUser) {
        AppUser savedUser = appUserDao.save(appUser);
        if (savedUser.hasClient()) {
            clientDao.findById(savedUser.getClientId())
                    .ifPresent(savedUser::setClient);
        }
        return savedUser;
    }

    public AppUser findById(Integer id) throws UsernameNotFoundException {
        AppUser appUser = appUserDao.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with id [%s] not found", id)));
        if (appUser.hasClient()) {
            clientDao.findById(appUser.getClientId())
                    .ifPresent(appUser::setClient);
        }
        return appUser;
    }

    public boolean userEmailExists(String email) {
        return appUserDao.existsByEmail(email);
    }

    public void resetPassword(String resetPasswordToken, AppUser appUser) {
        ResetPasswordToken resetPasswordTokenObject = ResetPasswordToken.builder()
                    .token(resetPasswordToken)
                    .appUserId(appUser.getId())
                    .creationTime(OffsetDateTime.now())
                    .build();
        tokens.put(appUser.getEmail(), resetPasswordTokenObject);
        emailService.sendPasswordResetLink(appUser, resetPasswordToken);
    }

    public void validateResetPasswordToken(String token) throws ResetPasswordTokenExpiredException, ResetPasswordTokenNotFoundException {
        for (ResetPasswordToken existingToken : tokens.values()) {
            if (existingToken.getToken().equals(token)) {
                if (existingToken.getCreationTime().isBefore(OffsetDateTime.now().minus(1, ChronoUnit.HOURS))) {
                    throw new ResetPasswordTokenExpiredException("This link validity expired");
                }
                return;
            }
            throw new ResetPasswordTokenNotFoundException("Token not found");
        }
    }

    @Scheduled(fixedRate = 86400000)
    private void cleanTokens() {
        this.tokens.entrySet().removeIf(element -> element.getValue().getCreationTime().isBefore(OffsetDateTime.now().minus(1, ChronoUnit.HOURS)));
    }

    public void setNewPassword(String resetPasswordToken, String newHashedPassword) {
        Optional<ResetPasswordToken> foundTokenObject = tokens.values().stream()
                .filter(entry -> resetPasswordToken.equals(entry.getToken()))
                .findFirst();
        if (!foundTokenObject.isPresent()) {
            throw new ResetPasswordTokenExpiredException("This token validity expired");
        }
        Optional<AppUser> appUser = appUserDao.findById(foundTokenObject.get().getAppUserId());
        if (!appUser.isPresent()) {
            throw new UserNotFoundException(String.format("User with ID [%s] not found", foundTokenObject.get().getAppUserId()));
        }
        appUser.get().setPasswd(newHashedPassword);
        appUserDao.save(appUser.get());
    }
}
