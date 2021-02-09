package md.ramaiana.foodmarket.service;

import md.ramaiana.foodmarket.dao.AppUserDao;
import md.ramaiana.foodmarket.dao.ClientDao;
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
    private final ClientDao clientDao;

    @Autowired
    public AppUserService(AppUserDao appUserDao, ClientDao clientDao) {
        this.appUserDao = appUserDao;
        this.clientDao = clientDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email [%s] not found", email)));
        if (appUser.hasClient()) {
            clientDao.findById(appUser.getClientId())
                    .ifPresent(appUser::setClient);
        }
        return appUser;
    }

    public AppUser registerNewUser(AppUser appUser) {
        AppUser savedUser = appUserDao.save(appUser);
        if (savedUser.hasClient()) {
            clientDao.findById(savedUser.getClientId())
                    .ifPresent(savedUser::setClient);
        }
        return savedUser;
    }

    public boolean userEmailExists(String email) {
        return appUserDao.existsByEmail(email);
    }

}
