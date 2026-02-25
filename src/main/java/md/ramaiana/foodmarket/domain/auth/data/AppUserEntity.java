package md.ramaiana.foodmarket.domain.auth.data;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * AppUser Entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("app_user")
public class AppUserEntity implements UserDetails {

  private UUID uuid = UUID.randomUUID();
  @Id
  private Integer id;
  @NonNull
  private String email;
  @NonNull
  private String passwd;
  @NonNull
  private Instant createdAt = Instant.now();
  @MappedCollection(idColumn = "user_id")
  private Set<UserRoleRef> userRoles = new HashSet<>();
  private Integer clientId;

  /**
   * Constructor.
   */
  public AppUserEntity(@NonNull String email, @NonNull String passwd) {
    this.email = email;
    this.passwd = passwd;
  }

  public void addRole(@NonNull Role role) {
    this.userRoles.add(new UserRoleRef(role));
  }

  public Set<Role> getRoles() {
    return userRoles.stream().map(UserRoleRef::getRole).collect(Collectors.toSet());
  }

  public boolean hasClient() {
    return clientId != null;
  }

  @Override
  @NonNull
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return userRoles.stream()
        .map(ref -> new SimpleGrantedAuthority("ROLE_" + ref.getRole().name()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return passwd;
  }

  @Override
  @NonNull
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
