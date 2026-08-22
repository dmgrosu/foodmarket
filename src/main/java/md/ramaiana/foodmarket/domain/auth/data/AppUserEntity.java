package md.ramaiana.foodmarket.domain.auth.data;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Getter
@Table("app_user")
public class AppUserEntity implements UserDetails {

  @Id
  private final Integer id;
  @NonNull
  private final String email;
  @NonNull
  private final String passwd;
  @NonNull
  private final Instant createdAt;
  @NonNull
  private final UserState state;
  @MappedCollection(idColumn = "user_id")
  private final Set<UserRoleRef> userRoles;
  @Column("client_id")
  private final AggregateReference<ClientEntity, Integer> client;

  @PersistenceCreator
  public AppUserEntity(Integer id, @NonNull String email, @NonNull String passwd, @NonNull Instant createdAt,
                       @NonNull UserState state, @NonNull Set<UserRoleRef> userRoles,
                       AggregateReference<ClientEntity, Integer> client) {
    this.id = id;
    this.email = email;
    this.passwd = passwd;
    this.createdAt = createdAt;
    this.state = state;
    this.userRoles = userRoles;
    this.client = client;
  }

  public AppUserEntity(@NonNull String email, @NonNull String passwd, @NonNull UserState state) {
    this(null, email, passwd, Instant.now(), state, new HashSet<>(), null);
  }

  public void addRole(@NonNull Role role) {
    this.userRoles.add(new UserRoleRef(role));
  }

  public Set<Role> getRoles() {
    return userRoles.stream().map(UserRoleRef::getRole).collect(Collectors.toSet());
  }

  public boolean hasClient() {
    return client != null;
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
    return !UserState.SUSPENDED.equals(state);
  }

  public boolean isActive() {
    return state.equals(UserState.ACTIVE);
  }

}
