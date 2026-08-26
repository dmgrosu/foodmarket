package md.ramaiana.foodmarket.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.NonNull;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Annotation used for defining RequestHandlers. A RequestHandler sits between a controller and one or
 * more UseCases, and owns the transaction boundary when a use case needs to combine a database write
 * with a call to an external system: the write runs inside a transaction, the external call runs after
 * it commits.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RequestHandler {

  /**
   * The value may indicate a suggestion for a logical component name, to be turned into a Spring
   * bean in case of an autodetected component.
   *
   * @return the suggested component name, if any (or empty String otherwise)
   */
  @NonNull
  @AliasFor(annotation = Component.class)
  String value() default "";
}
