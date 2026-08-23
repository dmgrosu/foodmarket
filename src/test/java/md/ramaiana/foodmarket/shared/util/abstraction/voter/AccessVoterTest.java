package md.ramaiana.foodmarket.shared.util.abstraction.voter;

import java.time.Instant;
import java.util.Set;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.UserRoleRef;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.ForbiddenException;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class AccessVoterTest {

  private final TestVoter voter = new TestVoter();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void assertUserIsAdmin_should_pass_for_admin_user() {
    authenticateAs(Set.of(Role.ADMIN));

    assertThatCode(voter::callAssertUserIsAdmin).doesNotThrowAnyException();
  }

  @Test
  void assertUserIsAdmin_should_reject_non_admin_user() {
    authenticateAs(Set.of(Role.USER));

    assertThatThrownBy(voter::callAssertUserIsAdmin).isInstanceOf(ForbiddenException.class);
  }

  @Test
  void assertUserIsAdmin_should_reject_unauthenticated_user() {
    SecurityContextHolder.getContext().setAuthentication(
        new AnonymousAuthenticationToken("key", "anonymousUser",
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS")))
    );

    assertThatThrownBy(voter::callAssertUserIsAdmin).isInstanceOf(UnauthorizedException.class);
  }

  @Test
  void assertUserIsAdmin_should_reject_when_no_authentication_present() {
    assertThatThrownBy(voter::callAssertUserIsAdmin).isInstanceOf(UnauthorizedException.class);
  }

  @Test
  void assertUserIsAuthenticated_should_pass_for_any_authenticated_user() {
    authenticateAs(Set.of(Role.USER));

    assertThatCode(voter::callAssertUserIsAuthenticated).doesNotThrowAnyException();
  }

  private void authenticateAs(Set<Role> roles) {
    AppUserEntity user = new AppUserEntity(1, "user@example.com", "hash", Instant.now(), UserState.ACTIVE,
        roles.stream().map(UserRoleRef::new).collect(java.util.stream.Collectors.toSet()), null);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities()));
  }

  private static class TestVoter extends AccessVoter {
    void callAssertUserIsAdmin() {
      assertUserIsAdmin();
    }

    void callAssertUserIsAuthenticated() {
      assertUserIsAuthenticated();
    }
  }
}
