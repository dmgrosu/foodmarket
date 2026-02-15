package md.ramaiana.foodmarket.shared.enums;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum Role {
  ADMIN("admin"),
  USER("user");

  private final String dbValue;

  Role(@NonNull String dbValue) {
    this.dbValue = dbValue;
  }
}
