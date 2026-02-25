package md.ramaiana.foodmarket.shared.exception.http;

import lombok.NonNull;

/**
 * Exception thrown when a resource is not found.
 */
public class NotFoundException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public NotFoundException(@NonNull String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The exception message.
   * @param cause   The cause of the exception.
   */
  public NotFoundException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}