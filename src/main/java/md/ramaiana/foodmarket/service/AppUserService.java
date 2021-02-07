package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/6/21
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
        return appUserDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email [%s] not found", email)));
    }

    public AppUser registerNewUser(AppUser appUser) {
        return appUserDao.save(appUser);
    }

    public boolean userEmailExists(String email) {
        return appUserDao.existsByEmail(email);
    }

}
