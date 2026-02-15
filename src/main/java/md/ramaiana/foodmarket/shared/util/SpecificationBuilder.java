package md.ramaiana.foodmarket.shared.util;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class used to build complex specifications.
 *
 * @param <T> The return type of the specification.
 */
public class SpecificationBuilder<@NonNull T> {

  @Nullable
  private Specification<@NonNull T> specification;

  /**
   * Add specification with an AND statement.
   *
   * @param specification The specification to add.
   */
  public void and(@NonNull Specification<@NonNull T> specification) {
    if (this.specification == null) {
      this.specification = specification;
    } else {
      this.specification = this.specification.and(specification);
    }
  }

  /**
   * Add specification with an OR statement.
   *
   * @param specification The specification to add.
   */
  public void or(@NonNull Specification<@NonNull T> specification) {
    if (this.specification == null) {
      this.specification = specification;
    } else {
      this.specification = this.specification.or(specification);
    }
  }

  /**
   * Build the specification.
   *
   * @return The specification. Might be null.
   */
  @Nullable
  public Specification<@NonNull T> build() {
    return this.specification;
  }

  /**
   * Build the specification.
   *
   * @return The specification. An empty specification if none were added.
   */
  @NonNull
  public Specification<@NonNull T> buildOrDefault() {
    if (this.specification == null) {
      return Specification.unrestricted();
    }
    return this.specification;
  }
}
