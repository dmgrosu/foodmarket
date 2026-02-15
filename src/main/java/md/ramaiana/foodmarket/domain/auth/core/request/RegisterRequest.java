package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotNull
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotNull
  @NotBlank(message = "Password is required")
  private String password;

  @Nullable
  private Integer clientId;
}
