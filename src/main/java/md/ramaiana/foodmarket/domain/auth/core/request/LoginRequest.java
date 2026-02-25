package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Login request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NonNull
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NonNull
  @NotBlank(message = "Password is required")
  private String password;
}
