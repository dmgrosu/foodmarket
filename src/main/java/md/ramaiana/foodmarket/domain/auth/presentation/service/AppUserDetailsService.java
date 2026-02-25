package md.ramaiana.foodmarket.domain.auth.presentation.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AppUserFindByEmailUseCase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService implementation that delegates to UseCases.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserFindByEmailUseCase appUserFindByEmailUseCase;

  @NonNull
  @Override
  public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
    try {
      return appUserFindByEmailUseCase.execute(email);
    } catch (Exception e) {
      throw new UsernameNotFoundException(String.format("User with email [%s] not found", email));
    }
  }
}
