package md.ramaiana.foodmarket.domain.auth.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Wrapper for Role enum, used by Spring Data JDBC for the app_user_roles table.
 */
@Getter
@EqualsAndHashCode(of = "role")
@Table("app_user_role")
public class UserRoleRef {

  private final Role role;

  @PersistenceCreator
  public UserRoleRef(@NonNull Role role) {
    this.role = role;
  }

}
