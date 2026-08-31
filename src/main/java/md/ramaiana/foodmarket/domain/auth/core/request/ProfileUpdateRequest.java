package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

/**
 * The fields a user may change on their own profile. Email is not among them — changing it would
 * require re-confirming the new address.
 */
public record ProfileUpdateRequest(

    @Nullable
    @Size(max = 255, message = "First name is too long")
    String firstName,

    @Nullable
    @Size(max = 255, message = "Last name is too long")
    String lastName,

    /**
     * An i18next language tag. Unknown tags fall back to RU rather than failing, matching both
     * registration and the frontend's fallbackLng.
     */
    @NonNull
    @NotBlank(message = "Language is required")
    String language
) {
}
