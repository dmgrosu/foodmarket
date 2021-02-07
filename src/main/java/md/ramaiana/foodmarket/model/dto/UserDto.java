package md.ramaiana.foodmarket.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * @author Dmitri Grosu, 2/7/21
 */
@Value
@Builder
@AllArgsConstructor
public class UserDto {
    Integer userId;
    String email;
    String passwd;
}
