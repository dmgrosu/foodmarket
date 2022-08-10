package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@Builder
public class ResetPasswordToken {
    private String token;
    private OffsetDateTime creationTime;
    private Integer appUserId;
}
