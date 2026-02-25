package md.ramaiana.foodmarket.domain.auth.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Wrapper for Role enum, used by Spring Data JDBC for the app_user_roles table.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "role")
@Table("app_user_roles")
public class UserRoleRef {

  private Role role;

  public UserRoleRef(@NonNull Role role) {
    this.role = role;
  }
}
