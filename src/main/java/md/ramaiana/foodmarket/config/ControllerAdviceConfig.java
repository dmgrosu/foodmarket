package md.ramaiana.foodmarket.config;

import jakarta.validation.ConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.ForbiddenException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Configuration for controllers on how to handle certain exceptions.
 */
@Slf4j
@ControllerAdvice
public class ControllerAdviceConfig {

  private static final String MDC_REQUEST_ID_KEY = "requestId";

  /**
   * Handle the exception that is thrown when static resources are requested but not found. An example
   * for when this happens is when someone tries to visit the url of our api in the browser. The browser
   * will try to fetch a favicon, which is not hosted. This will result in a {@link NoResourceFoundException}.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull NoResourceFoundException ex) {
    HttpStatus status = HttpStatus.NOT_FOUND;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", "Resource not found");
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle the exception that is thrown when validating an object using the {@link jakarta.validation.Valid}
   * annotation.
   *
   * @param ex The exception.
   * @return The bad request response.
   */
  @NonNull
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", 400);
    body.put("error", "Bad Request");

    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
    String errorMessage;
    if (!fieldErrors.isEmpty()) {
      FieldError fieldError = fieldErrors.getFirst();
      errorMessage = String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
    } else {
      errorMessage = "Bad request";
    }
    body.put("message", errorMessage);

    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle the exception that is thrown when validating method parameters using the
   * {@link org.springframework.validation.annotation.Validated} annotation.
   *
   * @param ex The exception.
   * @return The bad request response.
   */
  @NonNull
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull ConstraintViolationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", 400);
    body.put("error", "Bad Request");

    String fullMessage = ex.getMessage();
    String[] fullMessageParts = fullMessage.split(":");
    String[] pathParts = fullMessageParts[0].split("\\.");
    String fieldName = pathParts[pathParts.length - 1];
    String errorMessage = fullMessageParts.length > 1 ? fullMessageParts[1] : "No message available";
    body.put("message", String.format("%s: %s", fieldName, errorMessage.trim()));

    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle the exception that is thrown when:
   * - No request body is sent when one is required.
   * - A field could not be deserialized because the wrong type was sent.
   *
   * @param ex The exception.
   * @return The bad request response.
   */
  @NonNull
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull HttpMessageNotReadableException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", 400);
    body.put("error", "Bad Request");

    if (ex.getMessage() == null) {
      body.put("message", "Not valid");
    } else if (ex.getMessage().startsWith("Required request body is missing")) {
      body.put("message", "Required request body is missing");
    } else if (ex.getMessage().startsWith("JSON parse error: Cannot deserialize value of type")) {
      String[] parts = ex.getMessage().split(":");
      body.put("message", parts[0] + ":" + parts[1]);
    } else {
      body.put("message", ex.getMessage());
    }

    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle the exception that is thrown when:
   * - A required request parameter is not present.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(MissingServletRequestParameterException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull MissingServletRequestParameterException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle the exception that is thrown when:
   * - A request is made to an endpoint that does not support the request method.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull HttpRequestMethodNotSupportedException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle BadRequestException.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(BadRequestException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull BadRequestException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle ForbiddenException.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(ForbiddenException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull ForbiddenException ex) {
    HttpStatus status = HttpStatus.FORBIDDEN;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle NotFoundException.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull NotFoundException ex) {
    HttpStatus status = HttpStatus.NOT_FOUND;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle UnauthorizedException.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(UnauthorizedException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull UnauthorizedException ex) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getMessage());
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle a failed Spring Security authentication (bad credentials, unknown user). Without this the
   * generic handler below turns a mistyped password into a 500 "contact support".
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(AuthenticationException.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull AuthenticationException ex) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    // Deliberately generic: never reveal whether the email exists.
    body.put("message", "Invalid email or password");
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  /**
   * Handle any exception that is not already handled. Returns an internal server error.
   *
   * @param ex The exception.
   * @return The response.
   */
  @NonNull
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<@NonNull Map<String, Object>> handle(@NonNull Exception ex) {
    // Generate error id
    UUID errorId = UUID.randomUUID();

    // Log the exception
    log.error("(ErrorId: {}) {}", errorId, ex.getMessage(), ex);

    // Get the status
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    // Create response body
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("status", status.value());
    body.put("error", "Internal Server Error");
    body.put("message", String.format("Something went wrong, please contact support. (ErrorId: %s)", errorId));
    body.put("traceId", MDC.get(MDC_REQUEST_ID_KEY));

    // Return response
    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }
}