package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/6/21
 */
@AllArgsConstructor
@Getter
@Builder
@Table("app_user")
public class AppUser implements UserDetails {
    @Id
    Integer id;
    String email;
    String passwd;
    @Column("created_at")
    OffsetDateTime createdAt;
    @Column("deleted_at")
    OffsetDateTime deletedAt;
    @MappedCollection(idColumn = "user_id")
    Set<AppRole> roles;
    @Column("client_id")
    Integer clientId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(appRole -> new SimpleGrantedAuthority(appRole.getRole().name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.passwd;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return deletedAt == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return deletedAt == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(AppRole.builder()
                .role(role)
                .build());
    }

}
