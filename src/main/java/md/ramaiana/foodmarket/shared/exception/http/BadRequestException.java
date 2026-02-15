package md.ramaiana.foodmarket.shared.exception.http;

import lombok.NonNull;

/**
 * Exception thrown when a bad request is made.
 */
public class BadRequestException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public BadRequestException(@NonNull String message) {
    super(message);
  }
}