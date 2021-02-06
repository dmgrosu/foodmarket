package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @author Dmitri Grosu (dmitri.grosu@codefactorygroup.com), 2/6/21
 */
@Service
public class AppUserService implements UserDetailsService {

    private final AppUserDao appUserDao;

    @Autowired
    public AppUserService(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserDao.findByEmail(email);
        if (appUser == null) {
            throw new UsernameNotFoundException(String.format("User with email [%s] not found", email));
        }
        return new User(appUser.getEmail(), appUser.getPasswd(),
                appUser.getRoles().stream()
                        .map(appRole -> new SimpleGrantedAuthority(appRole.getRole().name()))
                        .collect(Collectors.toList()));
    }
}
