package md.ramaiana.foodmarket.shared.exception.http;

import lombok.NonNull;

/**
 * Exception thrown when a user is not authorized to access a resource.
 */
public class UnauthorizedException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public UnauthorizedException(@NonNull String message) {
    super(message);
  }
}