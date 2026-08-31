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

  /**
   * Optional: the sign-up form does not mark either name required, and users registered before these
   * columns existed have none.
   */
  @Nullable
  private String firstName;

  @Nullable
  private String lastName;

  @Nullable
  private Integer clientId;

  /**
   * The user's language tag as resolved by the frontend (i18next resolvedLanguage), e.g. "ro".
   * Optional: an unknown or absent tag falls back to Russian, matching the frontend's fallbackLng.
   */
  @Nullable
  private String language;
}
