package md.ramaiana.foodmarket.shared.exception.http;

import lombok.NonNull;

/**
 * Exception thrown when access to a resource is forbidden.
 */
public class ForbiddenException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public ForbiddenException(@NonNull String message) {
    super(message);
  }
}