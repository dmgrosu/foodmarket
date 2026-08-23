package md.ramaiana.foodmarket.domain.auth.core.response;

import jakarta.annotation.Nullable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.shared.enums.Role;

/**
 * Authentication response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  @NonNull
  private UserResponse user;

  @NonNull
  private String token;

  private int tokenTtl;

  /**
   * User data.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserResponse {
    private Integer id;

    @NonNull
    private String email;

    @NonNull
    private Set<Role> roles;

    @Nullable
    private ClientResponse client;

    public UserResponse(@NonNull AppUserEntity user, @Nullable ClientResponse client) {
      this.id = user.getId();
      this.email = user.getEmail();
      this.roles = user.getRoles();
      this.client = client;
    }
  }
}
