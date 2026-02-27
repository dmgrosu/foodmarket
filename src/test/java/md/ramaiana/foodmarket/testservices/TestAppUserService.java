package md.ramaiana.foodmarket.testservices;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestAppUserService {

  public static final String DEFAULT_PASSWORD = "password123";

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private int counter = 0;

  public TestAppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public AppUserEntity create() {
    counter++;
    return create("user" + counter + "@test.com", Role.USER);
  }

  @Transactional
  public AppUserEntity create(String email, Role... roles) {
    var user = new AppUserEntity(email, passwordEncoder.encode(DEFAULT_PASSWORD));
    for (Role role : roles) {
      user.addRole(role);
    }
    return appUserRepository.save(user);
  }

  @Transactional
  public AppUserEntity createWithClient(Integer clientId) {
    counter++;
    var user = new AppUserEntity("user" + counter + "@test.com", passwordEncoder.encode(DEFAULT_PASSWORD));
    user.addRole(Role.USER);
    user.setClientId(clientId);
    return appUserRepository.save(user);
  }

}
