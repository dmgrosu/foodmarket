package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Builder
@Table("app_user")
public class AppUser implements UserDetails {
    @Id
    @With
    private final Integer id;
    private final String email;
    private final String passwd;
    @Column("created_at")
    private final OffsetDateTime createdAt;
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;
    @Builder.Default
    @MappedCollection(idColumn = "user_id")
    private final Set<AppRole> roles = new HashSet<>();
    @Column("client_id")
    private final AggregateReference<@NonNull Client, @NonNull Integer> client;

    @NonNull
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

    @NonNull
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppUser appUser)) return false;
        return Objects.equals(id, appUser.id) && Objects.equals(email, appUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    public void addRole(Role role) {
        roles.add(AppRole.builder()
                .role(role)
                .build());
    }

    public boolean hasClient() {
        return client != null;
    }

}
